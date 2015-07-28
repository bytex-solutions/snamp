package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.internal.annotations.Internal;
import com.bytex.snamp.internal.annotations.ThreadSafe;

import javax.management.*;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * Represents JMX connectionHolder manager that provides reliable access to
 * MBean Server connectionHolder. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
final class JmxConnectionManager implements AutoCloseable {

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
            if (con != null) con.close();
        }
    }

    /**
     * Represents the watch dog that checks the JMX connectionHolder every N units of time.
     * This class cannot be inherited.
     */
    private static final class ConnectionWatchDog extends Repeater {
        private final Lock writeLock;
        private final ConnectionHolder connectionHolder;
        private final List<ConnectionEstablishedEventHandler> reconnectionHandlers;
        private final AtomicReference<IOException> problem;

        private ConnectionWatchDog(final TimeSpan period,
                                   final ConnectionHolder connection,
                                   final Lock writeLock) {
            super(period);
            this.writeLock = Objects.requireNonNull(writeLock);
            this.connectionHolder = connection;
            this.reconnectionHandlers = new Vector<>(4);
            this.problem = new AtomicReference<>(null);
        }

        /**
         * Reports about connection problem.
         *
         * @param e The connection problem.
         * @return {@literal true}, if there is not previous problems; otherwise, {@literal false}.
         */
        private boolean reportProblem(final IOException e) {
            return reportProblem(this.problem, e);
        }

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        private void reportProblemAndWait(final IOException e) throws InterruptedException {
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
                    JmxConnectorHelpers.log(Level.WARNING, "Unable to handle JMX reconnection %s", connectionHolder.factory, e);
                }
        }

        private static NotificationListener createConnectionTracker(final AtomicReference<IOException> problemHolder) {
            return new NotificationListener() {
                @Override
                public void handleNotification(final Notification notification, final Object handback) {
                    if (notification instanceof JMXConnectionNotification &&
                            (Objects.equals(notification.getType(), JMXConnectionNotification.NOTIFS_LOST) ||
                                    Objects.equals(notification.getType(), JMXConnectionNotification.FAILED)))
                        reportProblem(problemHolder, new IOException(notification.getMessage()));
                }
            };
        }

        /**
         * Provides some periodical action.
         */
        @Override
        protected void doAction() {
            writeLock.lock();
            try {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                final IOException problem = this.problem.get();
                final MBeanServerConnection server;
                if (problem != null) { //we have a network problem, force reconnection
                    final JMXConnector connector = connectionHolder.createConnection();
                    server = connector.getMBeanServerConnection();
                    connector.addConnectionNotificationListener(createConnectionTracker(this.problem), null, null);
                } else return;//no problem, return from method
                //notify about new connection
                onReconnection(server);
                this.problem.set(null);//erase the problem
            } catch (final IOException e) {
                JmxConnectorHelpers.log(Level.SEVERE, String.format("Failed to restore JMX connectionHolder %s", connectionHolder.factory), e);
                //save a problem
                reportProblem(e);
            } finally {
                writeLock.unlock();
            }
        }
    }

    private final ConnectionHolder connectionHolder;
    private final ConnectionWatchDog watchDog;
    private final Lock readLock;
    private final TimeSpan watchPeriod;

    JmxConnectionManager(final JmxConnectionFactory connectionString,
                         final long watchDogPeriod) {
        final ReentrantReadWriteLock coordinator = new ReentrantReadWriteLock();
        this.readLock = coordinator.readLock();
        connectionHolder = new ConnectionHolder(connectionString);
        watchDog = new ConnectionWatchDog(this.watchPeriod = new TimeSpan(watchDogPeriod), connectionHolder, coordinator.writeLock());
        //staring the watch dog
        watchDog.run();
    }

    @ThreadSafe(false)
    boolean connect() {
        try {
            return connectionHolder.createConnection() != null;
        } catch (final IOException e) {
            watchDog.reportProblem(e);
            return false;
        }
    }

    @ThreadSafe
    final <T> T handleConnection(final MBeanServerConnectionHandler<T> handler) throws Exception {
        readLock.lockInterruptibly();
        try {
            if (connectionHolder.isInitialized()) {
                final JMXConnector connector = connectionHolder.getConnection();
                return handler.handle(connector.getMBeanServerConnection());
            }
            else throw new IOException(String.format("Connection %s is not initialized", connectionHolder.factory));
        } catch (final IOException e) {
            watchDog.reportProblem(e);
            throw e;
        } finally {
            readLock.unlock();
        }
    }

    @ThreadSafe
    final void addReconnectionHandler(final ConnectionEstablishedEventHandler handler) {
        watchDog.reconnectionHandlers.add(handler);
    }

    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe
    final void removeReconnectionHandler(final ConnectionEstablishedEventHandler handler) {
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
    final void simulateConnectionAbort() throws IOException, InterruptedException {
        JMXConnector con = connectionHolder.connection;
        if (con != null)
            try {
                con.close();
            } finally {
                watchDog.reportProblemAndWait(new IOException("Simulate connection abort"));
            }
    }

    ObjectName resolveName(final ObjectName name) throws Exception {
        return handleConnection(new MBeanServerConnectionHandler<ObjectName>() {
            @Override
            public ObjectName handle(final MBeanServerConnection connection) throws IOException, JMException {
                final Set<ObjectInstance> beans = connection.queryMBeans(name, null);
                return beans.size() > 0 ? beans.iterator().next().getObjectName() : null;
            }
        });
    }

    @Override
    @ThreadSafe(false)
    public final void close() throws Exception {
        try {
            watchDog.reconnectionHandlers.clear();
            watchDog.stop(watchPeriod);
        } finally {
            connectionHolder.close();
        }
    }
}
