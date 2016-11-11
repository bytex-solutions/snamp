package com.bytex.snamp.connector.http;

import com.bytex.snamp.instrumentation.Measurement;

import java.util.HashMap;

/**
 * Provides conversion between {@link Measurement} and {@link com.bytex.snamp.connector.md.notifications.MeasurementNotification}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MeasurementConverter extends HashMap<Class<? extends Measurement>, ToMeasurementNotificationFunction> {
    private static final long serialVersionUID = -608166073781369733L;
}
