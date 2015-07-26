package com.bytex.snamp.connectors.groovy;

/**
 * Represents notification emitter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationEmitter extends AutoCloseable {
    void emitNotification(final String message);
    void emitNotification(final String message, final Object userData);
}
