package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.internal.UpgradableReadWriteLock;
import com.itworks.snamp.internal.annotations.Internal;
import com.itworks.snamp.internal.annotations.ThreadSafe;

import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents JMX connection manager that provides reliable access to
 * MBean Server connection. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
final class JmxConnectionManager implements Closeable {
    private static final Logger logger = JmxConnectorHelpers.getLogger();
    private final ReconnectionManager reconManager;

    private final static class ReconnectionManager implements javax.management.NotificationListener{
        private final JmxConnectionFactory serviceURL;
        private volatile JMXConnector connection;
        private final long connectionRetryCount;
        private final List<MBeanServerConnectionHandler<Void>> reconnectionHandlers;
        private final UpgradableReadWriteLock readWriteLock;

        private ReconnectionManager(final JmxConnectionFactory serviceURL, final long connectionRetryCount) {
            this.serviceURL = serviceURL;
            this.connection = null;
            this.connectionRetryCount = connectionRetryCount;
            readWriteLock = new UpgradableReadWriteLock();
            reconnectionHandlers = new Vector<>(5);
        }

        @Override
        public void handleNotification(final javax.management.Notification notification, final Object handback) {
            if (notification instanceof JMXConnectionNotification &&
                    (Objects.equals(notification.getType(), JMXConnectionNotification.NOTIFS_LOST) ||
                            Objects.equals(notification.getType(), JMXConnectionNotification.FAILED)))
                try {
                    readWriteLock.lockWrite();
                    connection = reconnect(serviceURL, reconnectionHandlers, getClass().getClassLoader(), connectionRetryCount, 0L);
                } catch (final IOException | InterruptedException e) {
                    logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                } finally {
                    readWriteLock.unlockWrite();
                }
        }

        private synchronized <T> T handleConnection(final MBeanServerConnectionHandler<T> handler) throws IOException, JMException, InterruptedException {
            final ClassLoader classLoader = getClass().getClassLoader();
            readWriteLock.lockRead();
            try {
                if (connection == null) {
                    //upgrade to write lock
                    readWriteLock.lockWrite();
                    try {
                        Thread.currentThread().setContextClassLoader(classLoader);
                        connection = serviceURL.createConnection();
                    } finally {
                        readWriteLock.unlockWrite();//downgrade to read lock
                    }
                } else unsubscribe(this, connection); //removes the listener
                //here we have only read lock
                return handler.handle(connection.getMBeanServerConnection());
            } catch (final IOException e) {
                //upgrade to write lock
                readWriteLock.lockWrite();
                try {
                    connection = reconnect(serviceURL, reconnectionHandlers, classLoader, connectionRetryCount, 0L);
                } finally {
                    readWriteLock.unlockWrite();//downgrade to read lock
                }
                return handleConnection(handler);
            } finally {
                //adds listener back
                subscribe(this, connection);
                //here we have always a read lock (release it)
                readWriteLock.unlockRead();
            }
        }

        private static void subscribe(final NotificationListener listener, final JMXConnector connection) {
            if (connection != null) {
                connection.addConnectionNotificationListener(listener, null, null);
            }
        }

        public static void unsubscribe(final NotificationListener listener, final JMXConnector connection) {
            if (connection != null)
                try {
                    connection.removeConnectionNotificationListener(listener);
                } catch (final ListenerNotFoundException ignored) {
                }
        }
    }

    public JmxConnectionManager(final JmxConnectionFactory connectionString, final long retryCount) {
        if (connectionString == null) throw new IllegalArgumentException("connectionString is null.");
        this.reconManager = new ReconnectionManager(connectionString, retryCount);
    }

    private static JMXConnector reconnect(final JmxConnectionFactory serviceURL,
                                          final Collection<MBeanServerConnectionHandler<Void>> reconnectionHandlers,
                                          final ClassLoader classLoader,
                                          final long connectionRetryCount,
                                          long attemptNumber) throws IOException {
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            final JMXConnector connection = serviceURL.createConnection();
            //raises reconnection event
            for (final MBeanServerConnectionHandler<Void> handler : reconnectionHandlers)
                try {
                    handler.handle(connection.getMBeanServerConnection());
                } catch (final JMException e) {
                    logger.log(Level.WARNING, e.getLocalizedMessage(), e);
                }
            return connection;
        } catch (final IOException e) {
            attemptNumber += 1;
            if (attemptNumber > connectionRetryCount)
                throw e;
            else return reconnect(serviceURL, reconnectionHandlers, classLoader, connectionRetryCount, attemptNumber);
        }
    }

    @ThreadSafe
    public final <T> T handleConnection(final MBeanServerConnectionHandler<T> handler) throws Exception {
        return reconManager.handleConnection(handler);
    }

    @ThreadSafe
    public final void addReconnectionHandler(final MBeanServerConnectionHandler<Void> handler) {
        reconManager.reconnectionHandlers.add(handler);
    }

    @SuppressWarnings("UnusedDeclaration")
    @ThreadSafe
    public final void removeReconnectionHandler(final MBeanServerConnectionHandler<Void> handler) {
        reconManager.reconnectionHandlers.remove(handler);
    }

    /**
     * Simulates connection abort.
     * <p>
     *     Only for testing purposes only.
     * </p>
     * @throws java.io.IOException Unable to simulate connection abort.
     */
    @Internal
    public final void simulateConnectionAbort() throws IOException {
        JMXConnector con = reconManager.connection;
        if (con != null)
            try {
                con.close();
            } finally {
                reconManager.handleNotification(new JMXConnectionNotification(JMXConnectionNotification.FAILED, this, "", 0, "Simulate connection abort", null), null);
            }
    }

    @Override
    @ThreadSafe(false)
    public final void close() throws IOException {
        JMXConnector con = reconManager.connection;
        if (con != null) con.close();
    }
}
