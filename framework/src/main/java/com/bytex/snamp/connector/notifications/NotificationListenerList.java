package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.AbstractWeakEventListenerList;
import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.annotation.concurrent.ThreadSafe;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * Represents list of notification listeners.
 * <p>
 *     This class is thread-safe.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public final class NotificationListenerList extends AbstractWeakEventListenerList<NotificationListener, Notification> implements NotificationListener {
    /**
     * Adds a listener to this MBean.
     *
     * @param listener The listener object which will handle the
     * notifications emitted by the broadcaster.
     * @param filter The filter object. If filter is null, no
     * filtering will be performed before handling notifications.
     * @param handback An opaque object to be sent back to the
     * listener when a notification is emitted. This object cannot be
     * used by the Notification broadcaster object. It should be
     * resent unchanged with the notification to the listener.
     *
     * @exception IllegalArgumentException Listener parameter is null.
     *
     * @see #removeNotificationListener
     */
    public final void addNotificationListener(final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback) throws IllegalArgumentException {
        add(listener, l -> new NotificationListenerHolder(l, filter, handback));
    }

    /**
     * Removes a listener from this MBean.  If the listener
     * has been registered with different handback objects or
     * notification filters, all entries corresponding to the listener
     * will be removed.
     *
     * @param listener A listener that was previously added to this
     * MBean.
     *
     * @exception javax.management.ListenerNotFoundException The listener is not
     * registered with the MBean.
     *
     * @see #addNotificationListener
     * @see javax.management.NotificationEmitter#removeNotificationListener
     */
    public final void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        if (!remove(listener))
            throw JMExceptionUtils.listenerNotFound(listener);
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
    public final void handleNotification(final Notification notification, final Object handback) {
        forEach(listener -> listener.handleNotification(notification, handback));
    }
}
