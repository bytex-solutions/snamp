package com.itworks.snamp.adapters.http;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationSupport {
    InternalBroadcaster getBroadcaster(final String resourceName);
}