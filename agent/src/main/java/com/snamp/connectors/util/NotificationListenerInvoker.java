package com.snamp.connectors.util;

import static com.snamp.connectors.NotificationSupport.Notification;
import static com.snamp.connectors.NotificationSupport.NotificationListener;

/**
 * Represents invocation method for the collection of {@link com.snamp.connectors.NotificationSupport.NotificationListener}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see NotificationListenerInvokerFactory
 */
public interface NotificationListenerInvoker {
    /**
     * Invokes a collection of listeners.
     * @param n The notification to pass into listeners.
     * @param category Event category.
     * @param listeners An array of listeners to invoke.
     */
    public void invoke(final Notification n, final String category, final Iterable<? extends NotificationListener> listeners);
}
