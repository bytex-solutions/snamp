package com.bytex.snamp.gateway;

import java.util.EventListener;

/**
 * Represents alternative notification listener.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface NotificationListener extends EventListener {
    NotificationListener NO_OP = event -> {};
    /**
     * Handles notifications.
     * @param event Notification event.
     */
    void handleNotification(final NotificationEvent event);
}
