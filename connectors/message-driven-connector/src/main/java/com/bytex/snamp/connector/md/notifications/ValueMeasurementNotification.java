package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.ValueMeasurement;

import java.util.Objects;

/**
 * Represents notification with measurement of some scalar value.
 */
public abstract class ValueMeasurementNotification<M extends ValueMeasurement> extends MeasurementNotification<M> {
    private static final long serialVersionUID = 71426758878763361L;
    private final M measurement;

    ValueMeasurementNotification(final String type, final Object source, final M measurement, final String message) {
        super(type, source, message);
        this.measurement = Objects.requireNonNull(measurement);
    }

    public final Comparable<?> getMeasurementValue(){
        return measurement.getRawValue();
    }

    @Override
    public final M getMeasurement() {
        return measurement;
    }
}
