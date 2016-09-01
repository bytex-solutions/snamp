package com.bytex.snamp.connector.notifications.measurement;

import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;

/**
 * Represents modification of gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum GaugeModification implements IntBinaryOperator, DoubleBinaryOperator, LongBinaryOperator {
    /**
     * A new gauge value must be computed as addition of newly supplied and existing value.
     */
    SUM {
        @Override
        public double applyAsDouble(final double currentValue, final double newValue) {
            return currentValue + newValue;
        }

        @Override
        public int applyAsInt(final int currentValue, final int newValue) {
            return currentValue + newValue;
        }

        @Override
        public long applyAsLong(final long currentValue, final long newValue) {
            return currentValue + newValue;
        }
    },

    /**
     * A new gauge value must be computed as subtraction of newly supplied and existing value.
     */
    SUB {
        @Override
        public double applyAsDouble(final double currentValue, final double newValue) {
            return currentValue - newValue;
        }

        @Override
        public int applyAsInt(final int currentValue, final int newValue) {
            return currentValue - newValue;
        }

        @Override
        public long applyAsLong(final long currentValue, final long newValue) {
            return currentValue - newValue;
        }
    },

    /**
     * A new gauge value must be computed as max value between newly supplied and existing value.
     */
    MAX {
        @Override
        public double applyAsDouble(final double currentValue, final double newValue) {
            return Math.max(currentValue, newValue);
        }

        @Override
        public int applyAsInt(final int currentValue, final int newValue) {
            return Math.max(currentValue, newValue);
        }

        @Override
        public long applyAsLong(final long currentValue, final long newValue) {
            return Math.max(currentValue, newValue);
        }
    },

    /**
     * A new gauge value must be computed as min value between newly supplied and existing value.
     */
    MIN {
        @Override
        public double applyAsDouble(final double currentValue, final double newValue) {
            return Math.min(currentValue, newValue);
        }

        @Override
        public int applyAsInt(final int currentValue, final int newValue) {
            return Math.min(currentValue, newValue);
        }

        @Override
        public long applyAsLong(final long currentValue, final long newValue) {
            return Math.min(currentValue, newValue);
        }
    },

    /**
     * Existing value will be replaced with newly supplied value.
     */
    NEW_VALUE {
        @Override
        public double applyAsDouble(final double currentValue, final double newValue) {
            return newValue;
        }

        @Override
        public int applyAsInt(final int currentValue, final int newValue) {
            return newValue;
        }

        @Override
        public long applyAsLong(final long currentValue, final long newValue) {
            return newValue;
        }
    };

    /**
     * Applies gauge modification.
     * @param currentValue The current value of gauge.
     * @param newValue A new gauge value.
     * @return Updated gauge value.
     */
    @Override
    public abstract double applyAsDouble(final double currentValue, final double newValue);

    /**
     * Applies gauge modification.
     * @param currentValue The current value of gauge.
     * @param newValue A new gauge value.
     * @return Updated gauge value.
     */
    @Override
    public abstract int applyAsInt(final int currentValue, final int newValue);

    /**
     * Applies gauge modification.
     * @param currentValue The current value of gauge.
     * @param newValue A new gauge value.
     * @return Updated gauge value.
     */
    @Override
    public abstract long applyAsLong(final long currentValue, final long newValue);
}
