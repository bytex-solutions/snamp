package com.itworks.snamp.connectors.aggregator;

import javax.management.MBeanNotificationInfo;

/**
 * An interface used to enqueue notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface NotificationEnqueue {
    /**
     * Enqueues a new notification into the event queue.
     * @param metadata The metadata of the notification.
     * @param message  The human-readable message.
     * @param userData User-defined payload.
     */
    void sendNotification(final MBeanNotificationInfo metadata,
                 final String message,
                 final Object userData);
}
