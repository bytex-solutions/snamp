package com.bytex.snamp.connectors.mq.jms;

/**
 * Represents type of message in the queue.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum SnampMessageType {
    WRITE,
    NOTIFICATION;

    static final String STORAGE_KEY_HEADER = "snampStorageKey";
}
