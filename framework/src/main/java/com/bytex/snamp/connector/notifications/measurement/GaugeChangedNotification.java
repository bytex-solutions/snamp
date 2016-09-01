package com.bytex.snamp.connector.notifications.measurement;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;

/**
 * Represents instantaneous measurement of a numeric value.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GaugeChangedNotification<V extends Number & Comparable<V>> extends InstantValueChangedNotification<V> implements IntUnaryOperator, LongUnaryOperator, DoubleUnaryOperator {
    private static final long serialVersionUID = 2049386049639308637L;
    private GaugeModification modification;

    public GaugeChangedNotification(final String componentName, final String instanceName, final String message, final V value) {
        super(componentName, instanceName, message, value);
        modification = GaugeModification.NEW_VALUE;
    }

    public GaugeModification getModificationType(){
        return modification;
    }

    public void setModificationType(final GaugeModification value){
        modification = Objects.requireNonNull(value);
    }

    @Override
    public double applyAsDouble(final double currentValue) {
        return modification.applyAsDouble(currentValue, value.doubleValue());
    }

    @Override
    public int applyAsInt(final int currentValue) {
        return modification.applyAsInt(currentValue, value.intValue());
    }

    @Override
    public long applyAsLong(final long currentValue) {
        return modification.applyAsLong(currentValue, value.longValue());
    }
}
