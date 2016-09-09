package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;

import java.util.OptionalInt;

/**
 * Represents reservoir of statistically distributed data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Reservoir extends Stateful {
    /**
     * Gets size of this reservoir.
     *
     * @return The size of this reservoir.
     */
    int getSize();

    /**
     * Gets capacity of this reservoir.
     *
     * @return The capacity of this reservoir.
     */
    int getCapacity();

    /**
     * Gets arithmetic mean of the values in this reservoir.
     *
     * @return Arithmetic mean.
     */
    double getMean();

    /**
     * Finds location of the value in this reservoir.
     * @param value The value to find.
     * @return The location of the value in this reservoir
     */
    OptionalInt find(final Number value);

    /**
     * Computes a percent of values that are greater than or equal to the specified value.
     * @param value A value to compute.
     * @return A percent of values that are greater that or equal to the specified value.
     */
    double greaterThanOrEqualValues(final Number value);

    /**
     * Computes a percent of values that are less than or equal to the specified value.
     * @param value A value to compute.
     * @return A percent of values that are less that or equal to the specified value.
     */
    double lessThanOrEqualValues(final Number value);

    /**
     * Gets standard deviation of the values in this reservoir.
     *
     * @return The standard deviation of the values in this reservoir.
     */
    double getDeviation();

    double getQuantile(final double quantile);

    /**
     * Adds a new value to this reservoir.
     *
     * @param value A value to add.
     */
    void add(final Number value);
}
