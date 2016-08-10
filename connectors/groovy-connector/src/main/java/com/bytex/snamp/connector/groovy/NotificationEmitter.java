package com.bytex.snamp.connector.groovy;

/**
 * Represents notification emitter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface NotificationEmitter extends AutoCloseable {
    void emitNotification(final String message);
    void emitNotification(final String message, final Object userData);
}
