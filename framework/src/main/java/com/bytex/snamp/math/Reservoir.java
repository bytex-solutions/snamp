package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;

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
