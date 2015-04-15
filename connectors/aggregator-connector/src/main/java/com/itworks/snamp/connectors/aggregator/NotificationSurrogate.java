package com.itworks.snamp.connectors.aggregator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationSurrogate {
    final String message;
    final Object userData;

    NotificationSurrogate(final String message,
                          final Object userData){
        this.message = message;
        this.userData = userData;
    }
}
