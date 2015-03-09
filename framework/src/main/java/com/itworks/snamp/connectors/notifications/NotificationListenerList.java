package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.internal.annotations.ThreadSafe;
import com.itworks.snamp.jmx.JMExceptionUtils;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents list of notification listeners.
 * <p>
 *     This class is not thread-safe.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe(false)
public class NotificationListenerList extends LinkedList<NotificationListenerHolder> implements NotificationListener {
    private static final long serialVersionUID = 3905356215172606650L;

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
    public void addNotificationListener(final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback) throws java.lang.IllegalArgumentException{
        add(new NotificationListenerHolder(listener, filter, handback));
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
    public void removeNotificationListener(final NotificationListener listener)
            throws ListenerNotFoundException{
        final Iterator<NotificationListenerHolder> listeners = iterator();
        boolean removed = false;
        while (listeners.hasNext())
            if(removed |= listeners.next().equals(listener))
                listeners.remove();
        if(!removed)
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
    public void handleNotification(final Notification notification, final Object handback) {
        for(final NotificationListenerHolder holder: this)
            holder.handleNotification(notification, handback);
    }
}
