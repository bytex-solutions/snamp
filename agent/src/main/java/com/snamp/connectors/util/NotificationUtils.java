package com.snamp.connectors.util;

import com.snamp.SynchronizationEvent;
import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import com.snamp.connectors.Notification;
import com.snamp.connectors.NotificationListener;

import java.util.concurrent.TimeoutException;

import static com.snamp.SynchronizationEvent.Awaitor;

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
     * Adds a new notification listener and returns synchronous awaitor for it.
     * <p>
     *     You can use this method for synchronization with notification delivery.
     *     When notification accepted then infrastructure removes the subscribed synchronization listener
     *     from it.
     * </p>
     * @param connector The notification listener connector. Cannot be {@literal null}.
     * @param category The category of notifications to listen.
     * @param listener The notification filter. Cannot be {@literal null}.
     * @param <N> Type of notifications to listen.
     * @return Notification awaitor that can be used to obtain notification synchronously;
     * or {@literal null} if notifications for the specified event category is not supported
     * by connector.
     */
    public static <N extends Notification> Awaitor<N> createAwaitor(final ManagementConnector connector, final String category, final NotificationListener<N> listener){
        if(connector == null) throw new IllegalArgumentException("connector is null.");
        else if(listener == null) throw new IllegalArgumentException("listener is null.");
        final SynchronizationEvent<N> ev = new SynchronizationEvent<>();
        final Object listenerId = connector.subscribe(category, new NotificationListener<N>() {
            @Override
            public final boolean handle(final N notification) {
                try{
                    return listener != null ? listener.handle(notification) : true;
                }
                finally {
                    ev.fire(notification);
                }
            }
        });
        return listenerId != null ? new Awaitor<N>() {
            private final Awaitor<N> awaitor = ev.getAwaitor();

            @Override
            public N await(final TimeSpan timeout) throws TimeoutException, InterruptedException {
                try{
                    return awaitor.await(timeout);
                }
                finally {
                    connector.unsubscribe(listenerId);
                }
            }

            @Override
            public N await() throws InterruptedException {
                try{
                    return awaitor.await();
                }
                finally {
                    connector.unsubscribe(listenerId);
                }
            }
        }: null;
    }

    /**
     * Adds a new notification listener and returns synchronous awaitor for it.
     * <p>
     *     You can use this method for synchronization with notification delivery.
     *     When notification accepted then infrastructure removes the subscribed synchronization listener
     *     from it.
     * </p>
     * @param connector The notification listener connector. Cannot be {@literal null}.
     * @param category The category of notifications to listen.
     * @param <N> Type of notifications to listen.
     * @return Notification awaitor that can be used to obtain notification synchronously;
     * or {@literal null} if notifications for the specified event category is not supported
     * by connector.
     */
    public static <N extends Notification> Awaitor<N> createAwaitor(final ManagementConnector connector, final String category){
        return createAwaitor(connector, category, new NotificationListener<N>() {
            @Override
            public boolean handle(final N n) {
                return true;
            }
        });
    }
}
