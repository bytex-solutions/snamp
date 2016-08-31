package com.bytex.snamp.connector.notifications.advanced;

import com.bytex.snamp.jmx.WellKnownType;

import java.util.Objects;

/**
 * Represents notification with the new instant value.
 * @param <V> Type of the value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class InstantValueNotification<V extends Comparable<?>> extends MonitoringNotification {
    private static final long serialVersionUID = -7177935916249311678L;

    public static final String TYPE = "com.bytex.notifications.instantValue";

    private final V value;

    private InstantValueNotification(final String componentName,
                             final String instanceName,
                             final String message,
                             final V value) {
        super(TYPE, componentName, instanceName, message);
        this.value = Objects.requireNonNull(value);
    }

    public V getValue(){
        return value;
    }

    public WellKnownType getValueType(){
        return WellKnownType.fromValue(value);
    }

    public static <V extends Number & Comparable<V>> InstantValueNotification<V> of(final String componentName,
                                                                           final String instanceName,
                                                                           final String message,
                                                                           final V value){
        return new InstantValueNotification<>(componentName, instanceName, message, value);
    }

    public static InstantValueNotification<String> of(final String componentName,
                                                                                          final String instanceName,
                                                                                          final String message,
                                                                                          final String value){
        return new InstantValueNotification<>(componentName, instanceName, message, value);
    }

    public static InstantValueNotification<Character> of(final String componentName,
                                                            final String instanceName,
                                                            final String message,
                                                            final char value){
        return new InstantValueNotification<>(componentName, instanceName, message, value);
    }

    public static InstantValueNotification<Boolean> of(final String componentName,
                                                             final String instanceName,
                                                             final String message,
                                                             final boolean value){
        return new InstantValueNotification<>(componentName, instanceName, message, value);
    }
}
