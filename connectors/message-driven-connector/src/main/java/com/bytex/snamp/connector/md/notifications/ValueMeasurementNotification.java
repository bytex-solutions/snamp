package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.ValueMeasurement;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents notification with measurement of some scalar value.
 */
public final class ValueMeasurementNotification extends MeasurementNotification {
    public static final String TYPE = "com.bytex.snamp.measurement.value";
    private static final long serialVersionUID = 71426758878763361L;
    private final ValueMeasurement measurement;

    public ValueMeasurementNotification(final Object source, final ValueMeasurement measurement) {
        super(TYPE, source, String.format("Value of type %s supplied", measurement.getType().getSimpleName()));
        this.measurement = Objects.requireNonNull(measurement);
    }

    public Comparable<?> getMeasurementValue(){
        return measurement.getRawValue();
    }

    @Override
    public ValueMeasurement getMeasurement() {
        return measurement;
    }

    public boolean isMeasurement(final Class<? extends ValueMeasurement> measurementType){
        return measurementType.isInstance(measurementType);
    }

    public <M extends ValueMeasurement> Optional<M> getMeasurement(final Class<M> measurementType) {
        return measurementType.isInstance(measurement) ? Optional.of(measurementType.cast(measurement)) : Optional.empty();
    }
}
