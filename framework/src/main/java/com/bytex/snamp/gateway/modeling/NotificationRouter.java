package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;

import javax.annotation.Nonnull;
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
    private final String resourceName;

    /**
     * Initializes a new notification router.
     * <p>
     *     Note that the notification router holds a weak reference
     * @param resourceName Name of the managed resource bounded to this router.
     * @param metadata The metadata of the notification. Cannot be {@literal null}.
     * @param destination The notification acceptor.
     */
    public NotificationRouter(@Nonnull final String resourceName,
                              final MBeanNotificationInfo metadata,
                              final NotificationListener destination) {
        super(metadata);
        this.weakListener = new WeakReference<>(destination);
        this.resourceName = resourceName;
    }

    public final String getResourceName(){
        return resourceName;
    }

    protected NotificationEvent createNotificationEvent(final Notification notification,
                                                        final Object handback){
        return new NotificationEvent(resourceName, getMetadata(), notification);
    }

    /**
     * Routes accepted notification to the underlying listener.
     * @param notification The notification to route.
     * @param handback An object associated with this listener at subscription time.
     */
    @Override
    public final void handleNotification(final Notification notification, final Object handback) {
        final NotificationListener listener = weakListener.get();
        if (listener != null) listener.handleNotification(createNotificationEvent(notification, handback));
    }

    /**
     * Disconnects notification accessor from the managed resource.
     */
    @Override
    public final void close() {
        weakListener.clear();
        super.close();
    }
}
