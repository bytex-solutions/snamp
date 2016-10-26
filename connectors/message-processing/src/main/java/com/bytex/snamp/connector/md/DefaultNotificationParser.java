package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DefaultNotificationParser implements NotificationParser {
    @Override
    public MeasurementNotification parse(final Map<String, ?> headers, final Object body) {
        return null;
    }
}
