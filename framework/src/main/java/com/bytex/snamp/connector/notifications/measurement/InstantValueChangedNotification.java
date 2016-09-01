package com.bytex.snamp.connector.notifications.measurement;

import com.bytex.snamp.jmx.WellKnownType;

import java.util.Objects;

/**
 * Represents notification with the new instant value.
 * @param <V> Type of the value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class InstantValueChangedNotification<V extends Comparable<?>> extends MeasurementNotification {
    private static final long serialVersionUID = -7177935916249311678L;

    public static final String TYPE = "com.bytex.measurement.instantValueChanged";

    final V value;
    private final WellKnownType valueType;

    InstantValueChangedNotification(final String componentName,
                                    final String instanceName,
                                    final String message,
                                    final V value) {
        super(TYPE, componentName, instanceName, message);
        this.value = Objects.requireNonNull(value);
        this.valueType = WellKnownType.fromValue(value);
        assert valueType != null;
    }

    /**
     * Gets type of instant value.
     * @return Type of instant value.
     */
    public final WellKnownType getValueType() {
        return valueType;
    }
}
