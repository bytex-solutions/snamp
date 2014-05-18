package com.itworks.snamp.connectors.notifications;

import java.util.EventListener;

/**
 * Represents notification listener.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationListener extends EventListener {

    /**
     * Handles the specified notification.
     * @param listId An identifier of the subscription list.
     * @param n The notification to handle.
     * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
     */
    boolean handle(final String listId, final Notification n);
}
