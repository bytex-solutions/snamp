package com.bytex.snamp.connector.notifications;

import javax.annotation.concurrent.ThreadSafe;
import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents list of notification listeners.
 * <p>
 *     This class is thread-safe.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public class NotificationListenerList implements NotificationListener {
    private final List<NotificationListenerHolder> listeners;

    public NotificationListenerList(){
        listeners = new CopyOnWriteArrayList<>();
    }

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
        listeners.add(new NotificationListenerHolder(listener, filter, handback));
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
        if (!listeners.removeIf(holder -> holder.equals(listener)))
            throw JMExceptionUtils.listenerNotFound(listener);
    }

    /**
     * Intercepts notification.
     * @param notification The original notification.
     * @return The modified notification.
     */
    protected Notification intercept(final Notification notification){
        return notification;
    }

    /**
     * Submits listener invocation.
     * <p>
     *     You can override this method and submit
     *     listener invocation into the separated thread.
     *     By default, the listener is invoked in the caller thread.
     * @param listener The listener to invoke.
     * @param notification The notification to be passed into the listener.
     * @param handback An object associated with the listener at subscription time.
     */
    protected void handleNotification(final NotificationListener listener,
                                      final Notification notification,
                                      final Object handback){
        listener.handleNotification(notification, handback);
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
        listeners.forEach(holder -> holder.handleNotification(notification, handback));
    }

    public final void handleNotification(final NotificationListenerInvoker invoker,
                                      final Notification notification,
                                      final Object handback) {
        invoker.invoke(notification, handback, listeners);
    }

    /**
     * Removes all listeners from this list.
     */
    public final void clear() {
        listeners.clear();
    }
}
