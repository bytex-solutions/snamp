package com.bytex.snamp.connector.notifications;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.Objects;

/**
 * Represents notification listener holder in association with filter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class NotificationListenerHolder implements NotificationListener, NotificationFilter {
    private static final long serialVersionUID = 4627857440844836452L;
    private final NotificationListener listener;
    private final NotificationFilter filter;
    private final Object handback;

    NotificationListenerHolder(final NotificationListener listener,
                               final NotificationFilter filter,
                               final Object handback) throws IllegalArgumentException{
        if(listener == null) throw new IllegalArgumentException("listener is null.");
        this.listener = listener;
        this.filter = filter;
        this.handback = handback;
    }

    /**
     * Invoked before sending the specified notification to the listener.
     *
     * @param notification The notification to be sent.
     * @return <CODE>true</CODE> if the notification has to be sent to the listener, <CODE>false</CODE> otherwise.
     */
    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return filter == null || filter.isNotificationEnabled(notification);
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     * @param handback     An opaque object which helps the listener to associate
     *                     information regarding the MBean emitter. This object is passed to the
     *                     addNotificationListener call and resent, without modification, to the
     */
    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if (isNotificationEnabled(notification))
            listener.handleNotification(notification, handback == null ? this.handback : handback);
    }

    private boolean equals(final NotificationListener listener) {
        return this == listener || Objects.equals(this.listener, listener);
    }

    @Override
    public boolean equals(final Object listener) {
        return listener instanceof NotificationListener && equals((NotificationListener)listener);
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }
}
