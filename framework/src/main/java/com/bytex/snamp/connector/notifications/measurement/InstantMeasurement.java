package com.bytex.snamp.connector.notifications.measurement;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/**
 * Represents notification with the new instant value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class InstantMeasurement extends Measurement {
    private static final long serialVersionUID = -7177935916249311678L;
    /**
     * Represents type of this notification.
     */
    public static final String TYPE = "com.bytex.measurement.valueChanged";

    private abstract static class InstantMeasurementBuilder extends MeasurementBuilder<InstantMeasurement>{
        private ModificationType type = ModificationType.NEW_VALUE;

        public final InstantMeasurementBuilder setModificationType(final ModificationType value){
            type = Objects.requireNonNull(value);
            return this;
        }

        private void fill(final InstantMeasurement notification){
            notification.setTimeStamp(getTimeStamp());
            notification.setUserData(getUserData());
            notification.setSequenceNumber(getSequenceNumber(true));
        }

        @Override
        public final String getType() {
            return TYPE;
        }
    }

    public final static class StringMeasurementBuilder extends InstantMeasurementBuilder{
        private String value;

        private StringMeasurementBuilder(){
            value = "";
        }

        public StringMeasurementBuilder setValue(final String value){
            this.value = Objects.requireNonNull(value);
            return this;
        }

        @Override
        public InstantMeasurement get() {
            final InstantMeasurement result = ofString(getSource(), getMessage(), value);
            super.fill(result);
            return result;
        }
    }

    public final static class LongMeasurementBuilder extends InstantMeasurementBuilder{
        private long value;

        private LongMeasurementBuilder(){
            value = 0L;
        }

        public LongMeasurementBuilder setValue(final long value){
            this.value = value;
            return this;
        }

        @Override
        public InstantMeasurement get() {
            final InstantMeasurement result = ofLong(getSource(), getMessage(), value);
            super.fill(result);
            return result;
        }
    }

    public final static class DoubleMeasurementBuilder extends InstantMeasurementBuilder{
        private double value;

        private DoubleMeasurementBuilder(){
            value = 0L;
        }

        public DoubleMeasurementBuilder setValue(final double value){
            this.value = value;
            return this;
        }

        @Override
        public InstantMeasurement get() {
            final InstantMeasurement result = ofDouble(getSource(), getMessage(), value);
            super.fill(result);
            return result;
        }
    }

    public final static class BooleanMeasurementBuilder extends InstantMeasurementBuilder{
        private boolean value;

        private BooleanMeasurementBuilder(){
            value = false;
        }

        public BooleanMeasurementBuilder setValue(final boolean value){
            this.value = value;
            return this;
        }

        @Override
        public InstantMeasurement get() {
            final InstantMeasurement result = ofBoolean(getSource(), getMessage(), value);
            super.fill(result);
            return result;
        }
    }

    private ModificationType modification;

    private InstantMeasurement(final Object source,
                               final String message) {
        super(TYPE, source, message);
        modification = ModificationType.NEW_VALUE;
    }

    /**
     * Constructs a new builder for {@link String} instant measurement.
     * @return A new builder.
     */
    public static StringMeasurementBuilder builderForString(){
        return new StringMeasurementBuilder();
    }

    /**
     * Constructs a new builder for {@code long} instant measurement.
     * @return A new builder.
     */
    public static LongMeasurementBuilder builderForLong(){
        return new LongMeasurementBuilder();
    }

    /**
     * Constructs a new builder for {@code double} instant measurement.
     * @return A new builder.
     */
    public static DoubleMeasurementBuilder builderForDouble(){
        return new DoubleMeasurementBuilder();
    }

    /**
     * Constructs a new builder for {@code boolean} instant measurement.
     * @return A new builder.
     */
    public static BooleanMeasurementBuilder builderForBoolean(){
        return new BooleanMeasurementBuilder();
    }

    /**
     * Determines whether this notification contains {@code double} value.
     * @return {@literal true}, if this notification contains {@code double} value.
     */
    public boolean isFloatingPoint(){
        return false;
    }

    /**
     * Determines whether this notification contains {@code boolean} value.
     * @return {@literal true}, if this notification contains {@code boolean} value.
     */
    public boolean isFlag(){
        return false;
    }

    /**
     * Determines whether this notification contains {@code long} value.
     * @return {@literal true}, if this notification contains {@code long} value.
     */
    public boolean isInteger(){
        return false;
    }

    /**
     * Determines whether this notification contains {@link String} value.
     * @return {@literal true}, if this notification contains {@link String} value.
     */
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

    public OptionalDouble applyAsDouble(final double currentValue){
        return OptionalDouble.empty();
    }

    public OptionalLong applyAsLong(final long currentValue){
        return OptionalLong.empty();
    }

    public Optional<Boolean> applyAsBoolean(final boolean currentValue){
        return Optional.empty();
    }

    public Optional<String> applyAsString(final String currentValue){
        return Optional.empty();
    }

    private static InstantMeasurement ofString(final Object source,
                                               final String message,
                                               final String value){
        return new InstantMeasurement(source, message){
            private static final long serialVersionUID = 6782560266045306772L;

            @Override
            public boolean isString() {
                return true;
            }

            @Override
            public Optional<String> applyAsString(final String currentValue) {
                return Optional.of(super.modification.apply(currentValue, value));
            }
        };
    }

    private static InstantMeasurement ofLong(final Object source,
                                            final String message,
                                            final long value){
        return new InstantMeasurement(source, message){
            private static final long serialVersionUID = -1182708770615184076L;

            @Override
            public boolean isInteger() {
                return true;
            }

            @Override
            public OptionalDouble applyAsDouble(final double currentValue) {
                return OptionalDouble.of(super.modification.apply(currentValue, value));
            }

            @Override
            public OptionalLong applyAsLong(final long currentValue) {
                return OptionalLong.of(super.modification.apply(currentValue, value));
            }
        };
    }

    private static InstantMeasurement ofBoolean(final Object source,
                                               final String message,
                                               final boolean value){
        return new InstantMeasurement(source, message){
            private static final long serialVersionUID = 4958445472595461806L;

            @Override
            public boolean isFlag() {
                return true;
            }

            @Override
            public OptionalDouble applyAsDouble(final double currentValue) {
                return OptionalDouble.of(super.modification.apply(currentValue, value ? 1D : 0D));
            }

            @Override
            public OptionalLong applyAsLong(final long currentValue) {
                return OptionalLong.of(super.modification.apply(currentValue, value ? 1L : 0L));
            }

            @Override
            public Optional<Boolean> applyAsBoolean(final boolean currentValue) {
                return Optional.of(super.modification.apply(currentValue, value));
            }
        };
    }

    private static InstantMeasurement ofDouble(final Object source,
                                              final String message,
                                              final double value){
        return new InstantMeasurement(source, message){
            private static final long serialVersionUID = -5581192241225949587L;

            @Override
            public boolean isFloatingPoint() {
                return true;
            }

            @Override
            public OptionalDouble applyAsDouble(final double currentValue) {
                return OptionalDouble.of(super.modification.apply(currentValue, value));
            }
        };
    }
}
