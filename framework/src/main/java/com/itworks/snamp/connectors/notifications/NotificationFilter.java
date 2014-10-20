package com.itworks.snamp.connectors.notifications;

/**
 * Represents notification filter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationFilter{
    /**
     * Determines whether the specified notification is allowed for handling.
     * @param resourceName The name of the emitting managed resource.
     * @param eventName The name of the notification.
     * @param notif The notification object.
     * @return {@literal true}, if notification is allowed for handling; otherwise, {@literal false}.
     */
    boolean isAllowed(final String resourceName,
                    final String eventName,
                    final Notification notif);
}
