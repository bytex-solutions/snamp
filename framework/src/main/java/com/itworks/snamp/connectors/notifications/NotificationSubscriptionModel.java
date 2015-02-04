package com.itworks.snamp.connectors.notifications;

/**
 * Represents model of the notification organization.
 * @author Roman Sakno
 * @version 1,0
 * @since 1.0
 */
public enum NotificationSubscriptionModel {
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
