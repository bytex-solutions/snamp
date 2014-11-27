package com.itworks.snamp.adapters.jmx;

import com.google.common.eventbus.Subscribe;

/**
 * Represents JMX notification handler.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JmxNotificationHandler {
    @Subscribe
    void processNotification(final JmxNotificationSurrogate notification);
}
