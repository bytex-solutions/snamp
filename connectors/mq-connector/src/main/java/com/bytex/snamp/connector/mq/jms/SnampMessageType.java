package com.bytex.snamp.connector.mq.jms;

/**
 * Represents type of message in the queue.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum SnampMessageType {
    WRITE,
    NOTIFICATION,
    ATTRIBUTE_CHANGED
}
