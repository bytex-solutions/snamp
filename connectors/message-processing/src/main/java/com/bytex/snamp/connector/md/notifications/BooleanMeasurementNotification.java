package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.BooleanUnaryOperator;
import com.bytex.snamp.instrumentation.BooleanMeasurement;

import java.util.function.BooleanSupplier;

/**
 * Represents measurement of {@code boolean} data type.
 * @since 2.0
 * @version 2.0
 */
public final class BooleanMeasurementNotification extends ValueMeasurementNotification<BooleanMeasurement> implements BooleanSupplier, BooleanUnaryOperator {
    private static final long serialVersionUID = -6963564838146442740L;

    public BooleanMeasurementNotification(final Object source, final BooleanMeasurement measurement) {
        super(source, measurement, "Boolean value supplied");
    }

    @Override
    public boolean getAsBoolean() {
        return getMeasurement().getValue();
    }

    @Override
    public boolean applyAsBoolean(final boolean existingValue) {
        return getMeasurement().getValue(existingValue);
    }
}
