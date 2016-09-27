package com.bytex.snamp.connector.md;

/**
 * How the MD-connector handles inbound notifications in cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum  NotificationProcessingModel {
    /**
     * Only one cluster node receive notifications at a time.
     * Examples of receiving channels: HTTP, HTTPS, JMS queue
     */
    UNICAST,

    /**
     * All cluster nodes receive notifications at a time.
     * Examples of receiving channels: JMX topic
     */
    MULTICAST
}
