package com.itworks.snamp.adapters.http;

import org.atmosphere.cpr.Broadcaster;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface NotificationSupport {
    Broadcaster getBroadcaster(final String resourceName);
}
