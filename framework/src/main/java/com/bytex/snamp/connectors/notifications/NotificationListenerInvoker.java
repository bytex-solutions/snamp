package com.bytex.snamp.connectors.notifications;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * Represents invocation method for the collection of {@link javax.management.NotificationListener}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 * @see NotificationListenerInvokerFactory
 */
public interface NotificationListenerInvoker {
    /**
     * Invokes a collection of listeners.
     * @param n The notification to pass into listeners.
     * @param handback Additional object passed to the notification listener.
     * @param listeners An array of listeners to invoke.
     */
    void invoke(final Notification n,
                final Object handback,
                final Iterable<? extends NotificationListener> listeners);
}
