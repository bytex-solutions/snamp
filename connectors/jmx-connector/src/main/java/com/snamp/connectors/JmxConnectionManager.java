package com.snamp.connectors;

import com.snamp.internal.Internal;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.SynchronizationType;
import com.snamp.internal.ThreadSafety;

import javax.management.*;
import javax.management.remote.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;

/**
 * Represents JMX connection manager that provides reliable access to
 * MBean Server connection. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Internal
final class JmxConnectionManager implements Closeable {
    public static final String RETRY_COUNT_PROPERTY = "retryCount";
    private final JMXServiceURL serviceURL;
    private final Map<String, Object> connectionProperties;
    private JMXConnector connection;
    private final Object coordinator;
    private final long connectionRetryCount;
    private final Logger logger;
    private final List<MBeanServerConnectionHandler<Void>> reconnectionHandlers;
    private final ConnectionListener listener;

    /**
     * Represents Management Bean connection handler.
     * @param <T> Type of the connection handling result.
     */
    public static interface MBeanServerConnectionHandler<T> extends EventListener {
        /**
         * Extracts object from the connection,
         * @param connection
         * @return
         * @throws java.io.IOException
         * @throws javax.management.JMException
         */
        public T handle(final MBeanServerConnection connection) throws IOException, JMException;
    }

    private final class ConnectionListener implements javax.management.NotificationListener{
        @Override
        public final void handleNotification(final javax.management.Notification notification, final Object handback) {
            if(notification instanceof JMXConnectionNotification &&
                    (Objects.equals(notification.getType(), JMXConnectionNotification.NOTIFS_LOST) ||
                            Objects.equals(notification.getType(), JMXConnectionNotification.FAILED)))
                reconnect(0L);
        }

        public final boolean subscribe(){
            if(connection != null){
                connection.addConnectionNotificationListener(this, null, null);
                return true;
            }
            else return false;
        }

        public final boolean unsubscribe(){
            if(connection != null)
                try {
                    connection.removeConnectionNotificationListener(this);
                    return true;
                }
                catch (final ListenerNotFoundException e) {
                    return false;
                }
            else return false;
        }
    }

    public JmxConnectionManager(final Logger log, final JMXServiceURL connectionString, final Map<String, Object> connectionProperties){
        if(connectionString == null) throw new IllegalArgumentException("connectionString is null.");
        this.serviceURL = connectionString;
        this.connectionProperties = connectionProperties != null ? Collections.unmodifiableMap(connectionProperties) : new HashMap<String, Object>();
        //default retry count = 3
        this.connectionRetryCount = connectionProperties.containsKey(RETRY_COUNT_PROPERTY) ?
            Long.valueOf(Objects.toString(connectionProperties.get(RETRY_COUNT_PROPERTY), "3")) :
            3L;
        this.logger = log;
        this.reconnectionHandlers = new ArrayList<>(5);
        this.coordinator = new Object();
        this.connection = null;
        this.listener = new ConnectionListener();
    }

    private boolean reconnect(final long attemptNumber){
        //reconnection failed
        if(attemptNumber > connectionRetryCount){
            logger.warning("Lost connection to JMX server.");
            return false;
        }
        synchronized (coordinator){
            try {
                connection = JMXConnectorFactory.connect(serviceURL, connectionProperties);
                //raises reconnection event
                for(final MBeanServerConnectionHandler<Void> handler: reconnectionHandlers)
                    try{
                        handler.handle(connection.getMBeanServerConnection());
                    }
                    catch (final JMException e){
                        logger.log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
            }
            catch (final IOException e) {
                logger.log(Level.WARNING, e.getLocalizedMessage(), e);
                return reconnect(attemptNumber + 1);
            }
        }
        return true;
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final <T> T handleConnection(final MBeanServerConnectionHandler<T> handler, final T defaultValue){
        synchronized (coordinator){
            try {
                if(connection == null) connection = JMXConnectorFactory.connect(serviceURL, connectionProperties);
                else listener.unsubscribe(); //removes the listener
                return handler.handle(connection.getMBeanServerConnection());
            }
            catch (final IOException e) {
                logger.log(Level.WARNING, String.format("Failed to connect to the JMX server %s", serviceURL), e);
                return reconnect(0L) ?
                        handleConnection(handler, defaultValue):
                        defaultValue;
            }
            catch (final JMException e){
                logger.log(Level.WARNING, e.getLocalizedMessage(), e);
                return defaultValue;
            }
            finally {
                //adds listener back
                listener.subscribe();
            }
        }
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final void addReconnectionHandler(final MBeanServerConnectionHandler<Void> handler){
        synchronized (coordinator){
            reconnectionHandlers.add(handler);
        }
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final void removeReconnectionHandler(final MBeanServerConnectionHandler<Void> handler){
        synchronized (coordinator){
            reconnectionHandlers.remove(handler);
        }
    }

    /**
     * Simulates connection abort.
     * <p>
     *     Only for testing purposes only.
     * </p>
     */
    @Internal
    public final void simulateConnectionAbort(){
        if(connection != null)
            try {
                connection.close();
            }
            catch (final IOException e) {
                //do nothing
            }
            finally {
                connection = null;
                listener.handleNotification(new JMXConnectionNotification(JMXConnectionNotification.FAILED, this, "", 0, "Simulate connection abort", null), null);
            }
    }

    @Override
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.EXCLUSIVE_LOCK)
    public final void close() throws IOException {
        if(connection != null) connection.close();
    }
}
