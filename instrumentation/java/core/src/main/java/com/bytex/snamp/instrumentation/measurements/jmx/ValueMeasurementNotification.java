package com.bytex.snamp.instrumentation.measurements.jmx;

import com.bytex.snamp.instrumentation.measurements.*;

import java.util.Objects;

/**
 * Represents notification with measurement of some scalar value.
 */
public final class ValueMeasurementNotification<V extends ValueMeasurement> extends MeasurementNotification {

    public static final String TYPE = "com.bytex.snamp.measurement.value";
    private static final long serialVersionUID = 71426758878763361L;
    private final V measurement;

    public ValueMeasurementNotification(final Object source, final V measurement) {
        super(TYPE, source, String.format("Value of type %s supplied", measurement.getType().getSimpleName()));
        this.measurement = Objects.requireNonNull(measurement);
    }

    public static ValueMeasurementNotification<BooleanMeasurement> ofBool(final Object source, final boolean value){
        return new ValueMeasurementNotification<>(source, new BooleanMeasurement(value));
    }

    public static ValueMeasurementNotification<BooleanMeasurement> ofBool(final Object source){
        return new ValueMeasurementNotification<>(source, new BooleanMeasurement());
    }

    public static ValueMeasurementNotification<IntegerMeasurement> ofInt(final Object source, final long value){
        return new ValueMeasurementNotification<>(source, new IntegerMeasurement(value));
    }

    public static ValueMeasurementNotification<IntegerMeasurement> ofInt(final Object source){
        return new ValueMeasurementNotification<>(source, new IntegerMeasurement());
    }

    public static ValueMeasurementNotification<FloatingPointMeasurement> ofFP(final Object source, final double value){
        return new ValueMeasurementNotification<>(source, new FloatingPointMeasurement(value));
    }

    public static ValueMeasurementNotification<FloatingPointMeasurement> ofFP(final Object source){
        return new ValueMeasurementNotification<>(source, new FloatingPointMeasurement());
    }

    public static ValueMeasurementNotification<StringMeasurement> ofString(final Object source, final String value){
        return new ValueMeasurementNotification<>(source, new StringMeasurement(value));
    }

    public static ValueMeasurementNotification<StringMeasurement> ofString(final Object source){
        return new ValueMeasurementNotification<>(source, new StringMeasurement());
    }

    public Comparable<?> getValue(){
        return measurement.getRawValue();
    }

    @Override
    public V getMeasurement() {
        return measurement;
    }

    public boolean isMeasurement(final Class<? extends ValueMeasurement> measurementType){
        return measurementType.isInstance(measurement);
    }
}
