package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.Measurement;

import java.util.function.Function;

/**
 * Represents converter from {@link Measurement} to {@link MeasurementNotification}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface MeasurementConverter<M extends Measurement> extends Function<M, MeasurementNotification<?>> {
    /**
     * Converts {@link Measurement} into {@link MeasurementNotification}.
     * @param measurement A measurement to convert.
     * @return Notification container
     */
    @Override
    MeasurementNotification<?> apply(final M measurement);
}
