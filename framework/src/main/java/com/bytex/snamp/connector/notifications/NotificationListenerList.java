package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents list of notification listeners.
 * <p>
 *     This class is thread-safe.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public class NotificationListenerList extends ThreadSafeObject implements NotificationListener {
    private final List<NotificationListenerHolder> listeners = new LinkedList<>();

    public NotificationListenerList(){
        super(SingleResourceGroup.class);
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
        writeAccept(SingleResourceGroup.INSTANCE, new NotificationListenerHolder(listener, filter, handback), listeners::add);
    }

    private void removeNotificationListenerImpl(final NotificationListener listener) throws ListenerNotFoundException{
        final Iterator<NotificationListenerHolder> listeners = this.listeners.iterator();
        boolean removed = false;
        while (listeners.hasNext())
            if(removed |= listeners.next().equals(listener))
                listeners.remove();
        if(!removed)
            throw JMExceptionUtils.listenerNotFound(listener);
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
    public final void removeNotificationListener(final NotificationListener listener)
            throws ListenerNotFoundException {
        writeAccept(SingleResourceGroup.INSTANCE, listener, this::removeNotificationListenerImpl);
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
        readAccept(SingleResourceGroup.INSTANCE, listeners, l -> l.forEach(holder -> handleNotification(holder, intercept(notification), handback)));
    }

    public final void handleNotification(final NotificationListenerInvoker invoker,
                                      final Notification notification,
                                      final Object handback) {
        readAccept(SingleResourceGroup.INSTANCE, listeners, list -> invoker.invoke(notification, handback, list));
    }

    /**
     * Removes all listeners from this list.
     */
    public final void clear() {
        writeAccept(SingleResourceGroup.INSTANCE, listeners, List::clear);
    }
}
