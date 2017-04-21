package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.WeakEventListener;

import javax.annotation.Nonnull;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * Represents notification listener holder in association with filter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NotificationListenerHolder extends WeakEventListener<NotificationListener, Notification> {
    private final NotificationFilter filter;
    private final Object handback;

    NotificationListenerHolder(final NotificationListener listener,
                               final NotificationFilter filter,
                               final Object handback) throws IllegalArgumentException{
        super(listener);
        this.filter = filter;
        this.handback = handback;
    }

    /**
     * Invokes event listener and pass event state object into it.
     *
     * @param listener A listener used to handle event. Cannot be {@literal null}.
     */
    @Override
    protected void invoke(@Nonnull final NotificationListener listener, @Nonnull final Notification notification) {
        if(filter == null || filter.isNotificationEnabled(notification)) {
            final Object origin = notification.getSource();
            listener.handleNotification(notification, handback);
            notification.setSource(origin);
        }
    }
}
