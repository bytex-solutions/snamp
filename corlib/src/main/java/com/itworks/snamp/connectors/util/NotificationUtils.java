package com.itworks.snamp.connectors.util;

import com.itworks.snamp.SynchronizationEvent;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.NotificationSupport;
import com.itworks.snamp.connectors.NotificationSupport.Notification;
import com.itworks.snamp.connectors.NotificationSupport.NotificationListener;

import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.SynchronizationEvent.Awaitor;

/**
 * Represents utility methods that simplifies working with notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NotificationUtils {
    private NotificationUtils(){

    }

    /**
     * Represents notification listener that can be used to handle the notification
     * synchronously. This class cannot be inherited.
     * <p>
     *     The following example demonstrates how to use this class:
     *     <pre>{@code
     *     final SynchronizationListener listener = new SynchronizationListener();
     *     final Object listenerId = connector.subscribe("subs-list", listener);
     *     listener.getAwaitor().await(); //blocks the caller thread
     *     connector.unsubscribe(listenerId);
     *     }</pre>
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class SynchronizationListener implements NotificationListener{
        private final SynchronizationEvent<Notification> synchronizer = new SynchronizationEvent<>();

        /**
         * Handles the specified notification.
         *
         * @param n The notification to handle.
         * @param category The event category.
         * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
         */
        @Override
        public boolean handle(final Notification n, final String category) {
            synchronizer.fire(n);
            return true;
        }

        /**
         * Returns a new awaitor for this listener.
         * @return A new awaitor for this listener.
         */
        public final Awaitor<Notification> getAwaitor(){
            return synchronizer.getAwaitor();
        }
    }

    /**
     * Computes an identifier for the specified notification listener.
     * @param listener Notification listener for which ID should be generated.
     * @return An identifier of the notification listener.
     */
    public static final String generateListenerId(final NotificationListener listener){
        return listener != null ? Integer.toString(listener.hashCode()) : null;
    }

    /**
     * Adds a new notification listener and returns synchronous awaitor for it.
     * <p>
     *     You can use this method for synchronization with notification delivery.
     *     When notification accepted then infrastructure removes the subscribed synchronization listener
     *     from it.
     * </p>
     * @param connector The notification listener connector. Cannot be {@literal null}.
     * @param listenerId An identifier of the listener to attach.
     * @param listId An identifier of the subscription list.
     * @param listener The notification filter. Cannot be {@literal null}.
     * @return Notification awaitor that can be used to obtain notification synchronously;
     * or {@literal null} if notifications for the specified event category is not supported
     * by connector.
     */
    public static Awaitor<Notification> createAwaitor(final NotificationSupport connector, final String listenerId, final String listId, final NotificationListener listener){
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        else if(listener == null) throw new IllegalArgumentException("listener is null.");
        final SynchronizationEvent<Notification> ev = new SynchronizationEvent<>();
        return connector.subscribe(listenerId, listId, new NotificationListener() {
            @Override
            public final boolean handle(final Notification notification, final String category) {
                try{
                    return listener != null ? listener.handle(notification, category) : true;
                }
                finally {
                    ev.fire(notification);
                }
            }
        })? new Awaitor<Notification>() {
            private final Awaitor<Notification> awaitor = ev.getAwaitor();

            @Override
            public Notification await(final TimeSpan timeout) throws TimeoutException, InterruptedException {
                try{
                    return awaitor.await(timeout);
                }
                finally {
                    connector.unsubscribe(listenerId);
                }
            }

            @Override
            public Notification await() throws InterruptedException {
                try{
                    return awaitor.await();
                }
                finally {
                    connector.unsubscribe(listenerId);
                }
            }
        } : null;
    }

    /**
     * Adds a new notification listener and returns synchronous awaitor for it.
     * <p>
     *     You can use this method for synchronization with notification delivery.
     *     When notification accepted then infrastructure removes the subscribed synchronization listener
     *     from it.
     * </p>
     * @param connector The notification listener connector. Cannot be {@literal null}.
     * @param listenerId An identifier of the listener to attach.
     * @param listId An identifier of the subscription list.
     * @return Notification awaitor that can be used to obtain notification synchronously;
     * or {@literal null} if notifications for the specified event category is not supported
     * by connector.
     */
    public static Awaitor<Notification> createAwaitor(final NotificationSupport connector, final String listenerId, final String listId){
        return createAwaitor(connector, listenerId, listId, new NotificationListener() {
            @Override
            public boolean handle(final Notification n, final String category) {
                return true;
            }
        });
    }
}
