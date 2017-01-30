package com.bytex.snamp.connector.dsp.notifications;

import com.bytex.snamp.instrumentation.measurements.*;

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

    public static ValueMeasurementNotification ofBool(final Object source, final boolean value){
        return new ValueMeasurementNotification(source, new BooleanMeasurement(value));
    }

    public static ValueMeasurementNotification ofBool(final Object source){
        return new ValueMeasurementNotification(source, new BooleanMeasurement());
    }

    public static ValueMeasurementNotification ofInt(final Object source, final long value){
        return new ValueMeasurementNotification(source, new IntegerMeasurement(value));
    }

    public static ValueMeasurementNotification ofInt(final Object source){
        return new ValueMeasurementNotification(source, new IntegerMeasurement());
    }

    public static ValueMeasurementNotification ofFP(final Object source, final double value){
        return new ValueMeasurementNotification(source, new FloatingPointMeasurement(value));
    }

    public static ValueMeasurementNotification ofFP(final Object source){
        return new ValueMeasurementNotification(source, new FloatingPointMeasurement());
    }

    public static ValueMeasurementNotification ofString(final Object source, final String value){
        return new ValueMeasurementNotification(source, new StringMeasurement(value));
    }

    public static ValueMeasurementNotification ofString(final Object source){
        return new ValueMeasurementNotification(source, new StringMeasurement());
    }

    public Comparable<?> getMeasurementValue(){
        return measurement.getRawValue();
    }

    @Override
    public ValueMeasurement getMeasurement() {
        return measurement;
    }

    public boolean isMeasurement(final Class<? extends ValueMeasurement> measurementType){
        return measurementType.isInstance(measurement);
    }

    public <M extends ValueMeasurement> Optional<M> getMeasurement(final Class<M> measurementType) {
        return measurementType.isInstance(measurement) ? Optional.of(measurementType.cast(measurement)) : Optional.empty();
    }
}
