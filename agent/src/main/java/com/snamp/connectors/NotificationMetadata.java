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
     * Represents model of the notification organization.
     * @author Roman Sakno
     * @version 1,0
     * @since 1.0
     */
    public static enum NotificationModel{
        /**
         * Only single listener can be subscribed to the notification.
         */
        UNICAST,

        /**
         * Many listeners can be subscribed to the notification.
         * Additional information about listeners launching is not available
         */
        MULTICAST,

        /**
         * Many listeners can be subscribed to the notification.
         * Listeners will be executed sequentially in the single thread.
         */
        MULTICAST_SEQUENTIAL,

        /**
         * Many listeners can be subscribed to the notification.
         * Each listener will be executed in the separated thread.
         */
        MULTICAST_PARALLEL,
    }

    /**
     * Gets the category of the notification.
     * @return The category of the notification.
     */
    public String getCategory();

    /**
     * Gets listeners invocation model for this notification type.
     * @return Listeners invocation model for this notification type.
     */
    public NotificationModel getNotificationModel();

    /**
     * Returns the type descriptor for the specified attachment.
     * @param attachment The notification attachment.
     * @return The type descriptor for the specified attachment; or {@literal null} if the specified
     * attachment is not supported.
     */
    public ManagementEntityType getAttachmentType(final Object attachment);
}
