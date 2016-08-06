package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.adapters.NotificationEvent;
import com.bytex.snamp.adapters.NotificationListener;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.lang.ref.WeakReference;

/**
 * Represents notification router that routes the notification
 * to the underlying listener.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class NotificationRouter extends NotificationAccessor {
    private final WeakReference<NotificationListener> weakListener;

    /**
     * Initializes a new notification router.
     * <p>
     *     Note that the notification router holds a weak reference
     * @param metadata The metadata of the notification. Cannot be {@literal null}.
     * @param destination The notification acceptor.
     */
    public NotificationRouter(final MBeanNotificationInfo metadata,
                              final NotificationListener destination) {
        super(metadata);
        this.weakListener = new WeakReference<>(destination);
    }

    @Override
    public final void disconnected() {
        weakListener.clear();
    }

    /**
     * Intercepts notification.
     * <p>
     *     You can use this method to modify the original notification object
     *     before routing.
     * @param notification The notification.
     * @return The modified notification.
     */
    protected Notification intercept(final Notification notification){
        return notification;
    }

    protected NotificationEvent createNotificationEvent(final Notification notification,
                                                        final Object handback){
        return new NotificationEvent(getMetadata(), notification);
    }

    /**
     * Routes accepted notification to the underlying listener.
     * @param notification The notification to route.
     * @param handback An object associated with this listener at subscription time.
     */
    @Override
    public final void handleNotification(final Notification notification, final Object handback) {
        final NotificationListener listener = weakListener.get();
        if(listener != null) listener.handleNotification(createNotificationEvent(intercept(notification), handback));
    }
}
