package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.adapters.NotificationEvent;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.Internal;

import java.util.Objects;

/**
 * Represents notification listener that aggregates many notification listeners.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class MulticastNotificationListener extends ThreadSafeObject implements NotificationListener {
    private static final class NotificationListenerList extends WeakEventListenerList<NotificationListener, NotificationEvent>{
        private static final long serialVersionUID = 5751134745848417480L;

        private NotificationListenerList(){

        }

        private NotificationListenerList(final NotificationListenerList listeners){
            super(listeners);
        }

        @Override
        protected void invoke(final NotificationEvent event, final NotificationListener listener) {
            listener.handleNotification(event);
        }

        @Override
        public NotificationListenerList clone() {
            return new NotificationListenerList(this);
        }
    }

    private enum SimpleLockDescriptor{
        LISTENERS
    }

    private final Enum<?> listenerLock;
    private final NotificationListenerList listeners;

    protected <G extends Enum<G>> MulticastNotificationListener(final Class<G> lockDescriptor,
                                                                final Enum<G> listenerLock){
        super(lockDescriptor);
        this.listenerLock = Objects.requireNonNull(listenerLock);
        this.listeners = new NotificationListenerList();
    }

    /**
     * Initializes a new empty collection of notification listeners.
     */
    public MulticastNotificationListener(){
        this(SimpleLockDescriptor.class, SimpleLockDescriptor.LISTENERS);
    }

    /**
     * Adds a new notification listener to this collection.
     * @param listener A new notification listener to add. Cannot be {@literal null}.
     * @return {@literal true}, if listener is added successfully; otherwise, {@literal false}.
     */
    public final boolean addNotificationListener(final NotificationListener listener){
        try(final LockScope ignored = beginWrite(listenerLock)){
            return listeners.add(listener);
        }
    }

    public final boolean removeNotificationListener(final NotificationListener listener){
        try(final LockScope ignored = beginWrite(listenerLock)){
            return listeners.remove(listener);
        }
    }

    /**
     * Removes all listeners from this collection.
     */
    protected final void removeAll(){
        try(final LockScope ignored = beginWrite(listenerLock)){
            listeners.clear();
        }
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    @Internal
    public final void handleNotification(final NotificationEvent event) {
        try(final LockScope ignored = beginRead(listenerLock)){
            listeners.fire(event);
        }
    }
}
