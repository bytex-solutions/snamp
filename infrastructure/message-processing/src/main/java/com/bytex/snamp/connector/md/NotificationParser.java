package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import java.util.Map;

/**
 * Represents notification parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface NotificationParser {
    MeasurementNotification parse(final Map<String, ?> headers, final Object body);
}
