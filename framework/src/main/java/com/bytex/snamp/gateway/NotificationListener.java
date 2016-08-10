package com.bytex.snamp.gateway;

import java.util.EventListener;

/**
 * Represents alternative notification listener.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface NotificationListener extends EventListener {
    /**
     * Handles notifications.
     * @param event Notification event.
     */
    void handleNotification(final NotificationEvent event);
}
