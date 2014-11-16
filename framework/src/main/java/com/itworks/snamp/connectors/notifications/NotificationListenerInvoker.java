package com.itworks.snamp.connectors.notifications;

/**
 * Represents invocation method for the collection of {@link com.itworks.snamp.connectors.notifications.NotificationListener}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see NotificationListenerInvokerFactory
 */
public interface NotificationListenerInvoker {
    /**
     * Invokes a collection of listeners.
     * @param listId Subscription list identifier to pass into listeners.
     * @param n The notification to pass into listeners.
     * @param listeners An array of listeners to invoke.
     */
    public void invoke(final String listId,
                       final Notification n,
                       final Iterable<? extends NotificationListener> listeners);
}
