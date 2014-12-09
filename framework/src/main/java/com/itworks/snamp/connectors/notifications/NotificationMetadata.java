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
     * <p>
     *  When attachment type cannot be defined statically then this method returns {@literal null}.
     *  However, the {@link Notification#getAttachment()} may return {@link com.itworks.snamp.connectors.ManagedEntityValue}
     *  object which contains dynamically defined attachment type.
     * </p>
     * @return An attachment descriptor; or {@literal null} if attachment type cannot be detected statically.
     */
    ManagedEntityType getAttachmentType();
}