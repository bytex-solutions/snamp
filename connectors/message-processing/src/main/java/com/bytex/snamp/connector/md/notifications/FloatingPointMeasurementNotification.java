package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.FloatingPointMeasurement;

import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents measurement of {@code double} data type.
 * @since 2.0
 * @version 2.0
 */
public final class FloatingPointMeasurementNotification extends ValueMeasurementNotification<FloatingPointMeasurement> implements DoubleSupplier, DoubleUnaryOperator {
    public static final String TYPE = "com.bytex.snamp.measurement.realSupplied";
    private static final long serialVersionUID = -7087035677342085686L;

    public FloatingPointMeasurementNotification(Object source, FloatingPointMeasurement measurement) {
        super(TYPE, source, measurement, "Floating-point number supplied");
    }

    @Override
    public double getAsDouble() {
        return getMeasurement().getValue();
    }

    @Override
    public double applyAsDouble(final double existingValue) {
        return getMeasurement().getValue(existingValue);
    }
}
