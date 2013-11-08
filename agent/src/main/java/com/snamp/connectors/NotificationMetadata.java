package com.snamp.connectors;

import java.util.Map;

/**
 * Represents metadata of the notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationMetadata extends Map<String, String> {
    /**
     * Gets the type of the notification content.
     * @return The type of the notification content.
     */
    public NotificationContentTypeInfo getContentType();

    /**
     * Gets the category of the notification.
     * @return The category of the notification.
     */
    public String getCategory();
}
