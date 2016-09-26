package com.bytex.snamp.connector.notifications.measurement;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Represents notification with the new instant value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ValueChangedNotification extends MeasurementNotification implements LongFunction<OptionalLong>, DoubleFunction<OptionalDouble>, Function<String, Optional<String>> {
    private static final long serialVersionUID = -7177935916249311678L;

    /**
     * Represents type of this notification.
     */
    public static final String TYPE = "com.bytex.measurement.valueChanged";

    private ModificationType modification;

    private ValueChangedNotification(final String componentName,
                             final String instanceName,
                             final String message) {
        super(TYPE, componentName, instanceName, message);
        modification = ModificationType.NEW_VALUE;
    }

    /**
     * Determines whether this notification contains {@code boolean} value.
     * @return {@literal true}, if this notification contains {@code boolean} value.
     */
    public boolean isFloatingPoint(){
        return false;
    }

    public boolean isFlag(){
        return false;
    }

    public boolean isInteger(){
        return false;
    }

    public boolean isString(){
        return false;
    }

    /**
     * Gets modification type.
     * @return Modification type associated with this notification.
     */
    public final ModificationType getModificationType(){
        return modification;
    }

    /**
     * Sets modification type.
     * @param value A new modification type.
     */
    public final void setModificationType(final ModificationType value){
        modification = Objects.requireNonNull(value);
    }

    @Override
    public OptionalDouble apply(final double currentValue){
        return OptionalDouble.empty();
    }

    @Override
    public OptionalLong apply(final long currentValue){
        return OptionalLong.empty();
    }

    public Optional<Boolean> apply(final boolean currentValue){
        return Optional.empty();
    }

    @Override
    public Optional<String> apply(final String currentValue){
        return Optional.empty();
    }

    public static ValueChangedNotification ofString(final String componentName,
                                                    final String instanceName,
                                                    final String message,
                                                    final String value){
        return new ValueChangedNotification(componentName, instanceName, message){
            private static final long serialVersionUID = 6782560266045306772L;

            @Override
            public boolean isString() {
                return true;
            }

            @Override
            public Optional<String> apply(final String currentValue) {
                return Optional.of(super.modification.apply(currentValue, value));
            }
        };
    }

    public static ValueChangedNotification ofLong(final String componentName,
                                                  final String instanceName,
                                                  final String message,
                                                  final long value){
        return new ValueChangedNotification(componentName, instanceName, message){
            private static final long serialVersionUID = -1182708770615184076L;

            @Override
            public boolean isInteger() {
                return true;
            }

            @Override
            public OptionalDouble apply(final double currentValue) {
                return OptionalDouble.of(super.modification.apply(currentValue, value));
            }

            @Override
            public OptionalLong apply(final long currentValue) {
                return OptionalLong.of(super.modification.apply(currentValue, value));
            }
        };
    }

    public static ValueChangedNotification ofBoolean(final String componentName,
                                                     final String instanceName,
                                                     final String message,
                                                     final boolean value){
        return new ValueChangedNotification(componentName, instanceName, message){
            private static final long serialVersionUID = 4958445472595461806L;

            @Override
            public boolean isFlag() {
                return true;
            }

            @Override
            public OptionalDouble apply(final double currentValue) {
                return OptionalDouble.of(super.modification.apply(currentValue, value ? 1D : 0D));
            }

            @Override
            public OptionalLong apply(final long currentValue) {
                return OptionalLong.of(super.modification.apply(currentValue, value ? 1L : 0L));
            }

            @Override
            public Optional<Boolean> apply(final boolean currentValue) {
                return Optional.of(super.modification.apply(currentValue, value));
            }
        };
    }

    public static ValueChangedNotification ofDouble(final String componentName,
                                                    final String instanceName,
                                                    final String message,
                                                    final double value){
        return new ValueChangedNotification(componentName, instanceName, message){
            private static final long serialVersionUID = -5581192241225949587L;

            @Override
            public boolean isFloatingPoint() {
                return true;
            }

            @Override
            public OptionalDouble apply(final double currentValue) {
                return OptionalDouble.of(super.modification.apply(currentValue, value));
            }
        };
    }
}
