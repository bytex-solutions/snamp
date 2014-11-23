package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.connectors.ManagedEntityMetadata;
import com.itworks.snamp.connectors.ManagedEntityType;

/**
 * Represents metadata of the notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationMetadata extends ManagedEntityMetadata {

    /**
     * Gets the category of the notification.
     * @return The category of the notification.
     */
    String getCategory();

    /**
     * Gets listeners invocation model for this notification type.
     * @return Listeners invocation model for this notification type.
     */
    NotificationModel getNotificationModel();

    /**
     * Gets attachment descriptor that can be used to convert notification attachment
     * into well-known object.
     * @return An attachment descriptor; or {@literal null} if attachments are not supported.
     */
    ManagedEntityType getAttachmentType();
}