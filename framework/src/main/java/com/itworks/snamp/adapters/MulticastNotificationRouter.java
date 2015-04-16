package com.itworks.snamp.adapters;

import com.itworks.snamp.WeakEventListenerList;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents notification router which stores multiple destinations.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class MulticastNotificationRouter extends NotificationAccessor {
    private static final class NotificationListenerList extends WeakEventListenerList<NotificationListener, NotificationEvent>{
        private static final long serialVersionUID = 6338780247917429249L;

        private NotificationListenerList(){

        }

        @Override
        protected void invoke(final NotificationEvent event, final NotificationListener listener) {
            listener.handleNotification(event);
        }
    }

    private final NotificationListenerList listeners;
    private final ReadWriteLock synchronizer;

    /**
     * Initializes a new managed resource notification accessor.
     *
     * @param metadata The metadata of the managed resource notification. Cannot be {@literal null}.
     */
    public MulticastNotificationRouter(final MBeanNotificationInfo metadata) {
        super(metadata);
        listeners = new NotificationListenerList();
        synchronizer = new ReentrantReadWriteLock();
    }

    public final boolean addNotificationListener(final NotificationListener listener){
        final Lock writeLock = synchronizer.writeLock();
        writeLock.lock();
        try{
            return listeners.add(listener);
        }
        finally {
            writeLock.unlock();
        }
    }

    public final boolean removeNotificationListener(final NotificationListener listener){
        final Lock writeLock = synchronizer.writeLock();
        writeLock.lock();
        try{
            return listeners.remove(listener);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes all listeners.
     */
    @Override
    public final void disconnected() {
        final Lock writeLock = synchronizer.writeLock();
        writeLock.lock();
        try{
            listeners.clear();
        }
        finally {
            writeLock.unlock();
        }
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
    public final void handleNotification(final Notification notification,
                                         final Object handback) {
        final Lock readLock = synchronizer.readLock();
        readLock.lock();
        try{
            listeners.fire(createNotificationEvent(intercept(notification), handback));
        }
        finally {
            readLock.unlock();
        }
    }
}
