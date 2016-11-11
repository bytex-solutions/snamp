package com.bytex.snamp.connector.http;

import com.bytex.snamp.connector.md.notifications.MeasurementNotification;
import com.bytex.snamp.instrumentation.Measurement;

import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface ToMeasurementNotificationFunction<M extends Measurement> extends Function<M, MeasurementNotification> {
    @Override
    MeasurementNotification apply(final M measurement);
}
