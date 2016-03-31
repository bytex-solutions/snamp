package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.Internal;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.adapters.NotificationEvent;
import com.bytex.snamp.adapters.NotificationListener;

/**
 * Represents notification listener that aggregates many notification listeners.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class MulticastNotificationListener implements NotificationListener {
    private static final class NotificationListenerList extends WeakEventListenerList<NotificationListener, NotificationEvent>{
        private NotificationListenerList(){

        }

        @Override
        protected void invoke(final NotificationEvent event, final NotificationListener listener) {
            listener.handleNotification(event);
        }
    }

    private final NotificationListenerList listeners = new NotificationListenerList();
    /**
     * Adds a new notification listener to this collection.
     * @param listener A new notification listener to add. Cannot be {@literal null}.
     */
    public final void addNotificationListener(final NotificationListener listener) {
        listeners.add(listener);
    }

    public final boolean removeNotificationListener(final NotificationListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Removes all listeners from this collection.
     */
    protected final void removeAll(){
        listeners.clear();
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    @Internal
    public final void handleNotification(final NotificationEvent event) {
        listeners.fire(event);
    }
}
