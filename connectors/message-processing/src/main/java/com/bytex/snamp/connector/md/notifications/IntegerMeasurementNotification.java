package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.IntegerMeasurement;

import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;

/**
 * Represents measurement of {@code long} data type.
 * @since 2.0
 * @version 2.0
 */
public final class IntegerMeasurementNotification extends ValueMeasurementNotification<IntegerMeasurement> implements LongSupplier, LongUnaryOperator {
    private static final long serialVersionUID = 1741159938641920680L;

    public IntegerMeasurementNotification(Object source, IntegerMeasurement measurement) {
        super(source, measurement);
    }

    @Override
    public long getAsLong() {
        return getMeasurement().getValue();
    }

    @Override
    public long applyAsLong(final long existingValue) {
        return getMeasurement().getValue(existingValue);
    }
}
