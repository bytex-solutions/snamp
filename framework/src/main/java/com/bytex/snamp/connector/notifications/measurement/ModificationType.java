package com.bytex.snamp.connector.notifications.measurement;

import java.io.Serializable;

/**
 * Represents modification of gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum ModificationType implements Serializable {
    /**
     * A new gauge value must be computed as addition of newly supplied and existing value.
     */
    SUM {
        @Override
        double apply(final double currentValue, final double newValue) {
            return currentValue + newValue;
        }

        @Override
        long apply(final long currentValue, final long newValue) {
            return currentValue + newValue;
        }

        @Override
        boolean apply(final boolean currentValue, final boolean newValue) {
            return currentValue | newValue;
        }

        @Override
        String apply(final String currentValue, final String newValue) {
            return currentValue.concat(newValue);
        }
    },

    /**
     * A new gauge value must be computed as subtraction of newly supplied and existing value.
     */
    SUB {
        @Override
        double apply(final double currentValue, final double newValue) {
            return currentValue - newValue;
        }

        @Override
        long apply(final long currentValue, final long newValue) {
            return currentValue - newValue;
        }

        @Override
        boolean apply(final boolean currentValue, final boolean newValue) {
            //true - false = true
            //true - true = false
            //false - true = false
            //false - false = false
            return currentValue & !newValue;
        }

        @Override
        String apply(final String currentValue, final String newValue) {
            return null;
        }
    },

    /**
     * A new gauge value must be computed as max value between newly supplied and existing value.
     */
    MAX {
        @Override
        double apply(final double currentValue, final double newValue) {
            return Math.max(currentValue, newValue);
        }

        @Override
        long apply(final long currentValue, final long newValue) {
            return Math.max(currentValue, newValue);
        }

        @Override
        boolean apply(final boolean currentValue, final boolean newValue) {
            return newValue | currentValue;
        }

        @Override
        String apply(final String currentValue, final String newValue) {
            return currentValue.compareTo(newValue) > 0 ? currentValue : newValue;
        }
    },

    /**
     * A new gauge value must be computed as min value between newly supplied and existing value.
     */
    MIN {
        @Override
        double apply(final double currentValue, final double newValue) {
            return Math.min(currentValue, newValue);
        }

        @Override
        long apply(final long currentValue, final long newValue) {
            return Math.min(currentValue, newValue);
        }

        @Override
        boolean apply(final boolean currentValue, final boolean newValue) {
            return currentValue & newValue;
        }

        @Override
        String apply(final String currentValue, final String newValue) {
            return currentValue.compareTo(newValue) < 0 ? currentValue : newValue;
        }
    },

    /**
     * Existing value will be replaced with newly supplied value.
     */
    NEW_VALUE {
        @Override
        double apply(final double currentValue, final double newValue) {
            return newValue;
        }

        @Override
        long apply(final long currentValue, final long newValue) {
            return newValue;
        }

        @Override
        boolean apply(final boolean currentValue, final boolean newValue) {
            return newValue;
        }

        @Override
        String apply(final String currentValue, final String newValue) {
            return newValue;
        }
    };

    abstract double apply(final double currentValue, final double newValue);

    abstract long apply(final long currentValue, final long newValue);

    abstract boolean apply(final boolean currentValue, final boolean newValue);

    abstract String apply(final String currentValue, final String newValue);
}
