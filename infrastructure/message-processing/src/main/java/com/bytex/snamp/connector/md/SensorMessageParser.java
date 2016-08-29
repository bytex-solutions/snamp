package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.advanced.MonitoringNotification;

import java.util.Map;

/**
 * Transforms raw message from sensor into well-known SNAMP notification.
 * @since 2.0
 * @version 2.0
 */
public interface SensorMessageParser {
    MonitoringNotification parseMessage(final Map<String, ?> headers, final Object body);
}
