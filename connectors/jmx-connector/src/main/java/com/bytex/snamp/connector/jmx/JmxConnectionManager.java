package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.Internal;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LockManager;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.internal.Utils;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents JMX connectionHolder manager that provides reliable access to
 * MBean Server connectionHolder. This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Internal
final class JmxConnectionManager extends ThreadSafeObject implements AutoCloseable {
    private static final SingleResourceGroup CONNECTION_RESOURCE = SingleResourceGroup.INSTANCE;

    private static class ConnectionHolder implements Closeable {
        private final JmxConnectionFactory factory;
        private volatile JMXConnector connection;

        private ConnectionHolder(final JmxConnectionFactory factory) {
            this.factory = Objects.requireNonNull(factory);
            connection = null;
        }

        private JMXConnector createConnection() throws IOException {
            return connection = factory.createConnection();
        }

        private JMXConnector getConnection() {
            return connection;
        }

        private boolean isInitialized() {
            return connection != null;
        }

        @Override
        public void close() throws IOException {
            final JMXConnector con = connection;
            if (con != null) try {
                con.close();
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Represents the watch dog that checks the JMX connectionHolder every N units of time.
     * This class cannot be inherited.
     */
    private static final class ConnectionWatchDog extends Repeater {
        private final LockManager writeLock;
        private final ConnectionHolder connectionHolder;
        private final List<ConnectionEstablishedEventHandler> reconnectionHandlers;
        private final AtomicReference<IOException> problem;
        private final Logger logger;

        private ConnectionWatchDog(final Duration period,
                                   final ConnectionHolder connection,
                                   final LockManager writeLock,
                                   final Logger logger) {
            super(period);
            this.logger = Objects.requireNonNull(logger);
            this.writeLock = Objects.requireNonNull(writeLock);
            this.connectionHolder = connection;
            this.reconnectionHandlers = new Vector<>(4);
            this.problem = new AtomicReference<>();
        }

        /**
         * Reports about connection problem.
         *
         * @param e The connection problem.
         * @return {@literal true}, if there is not previous problems; otherwise, {@literal false}.
         */
        boolean reportProblem(final IOException e) {
            return reportProblem(this.problem, e);
        }

        void reportProblemAndWait(final IOException e) throws InterruptedException {
            reportProblem(problem, e);
            while (problem.get() != null)
                Thread.sleep(1);
        }

        private static boolean reportProblem(final AtomicReference<IOException> problemHolder, final IOException problem) {
            return problemHolder.compareAndSet(null, problem);
        }

        private void onReconnection(final MBeanServerConnection server) throws IOException {
            for (final ConnectionEstablishedEventHandler handler : reconnectionHandlers)
                try {
                    handler.handle(server);
                } catch (final JMException e) {
                    logger.log(Level.WARNING, String.format("Unable to handle JMX reconnection %s", connectionHolder.factory), e);
                }
        }

        @Override
        protected void doAction() throws InterruptedException, TimeoutException {
            try (final SafeCloseable ignored = writeLock.acquireLock(CONNECTION_RESOURCE, null)) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                final IOException problem = this.problem.get();
                final MBeanServerConnection server;
                if (problem != null) { //we have a network problem, force reconnection
                    final JMXConnector connector = connectionHolder.createConnection();
                    server = connector.getMBeanServerConnection();
                    connector.addConnectionNotificationListener((notification, handback) -> {
                        if (notification instanceof JMXConnectionNotification &&
                                (Objects.equals(notification.getType(), JMXConnectionNotification.NOTIFS_LOST) ||
                                        Objects.equals(notification.getType(), JMXConnectionNotification.FAILED)))
                            reportProblem(this.problem, new IOException(notification.getMessage()));
                    }, null, null);
                } else return;//no problem, return from method
                //notify about new connection
                onReconnection(server);
                this.problem.set(null);//erase the problem
            } catch (final IOException e) {
                logger.log(Level.SEVERE, String.format("Failed to restore JMX connectionHolder %s", connectionHolder.factory), e);
                //save a problem
                reportProblem(e);
            }
        }

        void terminate() throws TimeoutException, InterruptedException {
            close(getPeriod());
        }
    }

    private final ConnectionHolder connectionHolder;
    private ConnectionWatchDog watchDog;

    JmxConnectionManager(final JmxConnectionFactory connectionString,
                         final long watchDogPeriod,
                         final Logger logger) {
        super(SingleResourceGroup.class);
        connectionHolder = new ConnectionHolder(connectionString);
        watchDog = new ConnectionWatchDog(Duration.ofMillis(watchDogPeriod), connectionHolder, writeLock, logger);
        //staring the watch dog
        watchDog.run();
    }

    void connect() throws IOException {
        connectionHolder.createConnection();
        assert connectionHolder.isInitialized();
    }

    <T> T handleConnection(final MBeanServerConnectionHandler<T> handler) throws InterruptedException, JMException, IOException {
        try (final SafeCloseable ignored = readLock.acquireLock(CONNECTION_RESOURCE, null)) {
            if (connectionHolder.isInitialized()) {
                final JMXConnector connector = connectionHolder.getConnection();
                return handler.handle(connector.getMBeanServerConnection());
            } else throw new IOException(String.format("Connection %s is not initialized", connectionHolder.factory));
        } catch (final IOException e) {
            watchDog.reportProblem(e);
            throw e;
        } catch (final TimeoutException e) {
            throw new UncheckedTimeoutException(e);
        }
    }

    void addReconnectionHandler(final ConnectionEstablishedEventHandler handler) {
        watchDog.reconnectionHandlers.add(handler);
    }

    void removeReconnectionHandler(final ConnectionEstablishedEventHandler handler) {
        watchDog.reconnectionHandlers.add(handler);
    }

    /**
     * Simulates connectionHolder abort.
     * <p>
     * Only for testing purposes only.
     * </p>
     *
     * @throws java.io.IOException Unable to simulate connectionHolder abort.
     */
    @Internal
    void simulateConnectionAbort() throws IOException, InterruptedException {
        JMXConnector con = connectionHolder.connection;
        if (con != null)
            try {
                con.close();
            } finally {
                watchDog.reportProblemAndWait(new IOException("Simulate connection abort"));
            }
    }

    ObjectName resolveName(final ObjectName name) throws InterruptedException, JMException, IOException {
        return handleConnection(connection -> {
            final Set<ObjectInstance> beans = connection.queryMBeans(name, null);
            return beans.size() > 0 ? beans.iterator().next().getObjectName() : null;
        });
    }

    @Override
    public void close() throws Exception {
        watchDog.reconnectionHandlers.clear();
        try {
            Utils.closeAll(watchDog::terminate, connectionHolder);
        } finally {
            watchDog = null;
        }
    }
}
