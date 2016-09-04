package com.bytex.snamp.connector.metrics;

/**
 * Represents specialized version of 64-bit signed gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Gauge64 extends NumericGauge {
    /**
     * Gets maximum value ever presented.
     * @return Maximum value ever presented.
     */
    long getMaxValue();

    /**
     * Gets maximum value for the last period of time.
     * @param interval Period of time.
     * @return Maximum value of the last period of time.
     */
    long getLastMaxValue(final MetricsInterval interval);

    /**
     * The minimum value ever presented.
     * @return The minimum value ever presented.
     */
    long getMinValue();

    /**
     * Gets minimum value for the last period of time.
     * @param interval Period of time.
     * @return Minimum value for the last period of time.
     */
    long getLastMinValue(final MetricsInterval interval);

    /**
     * The last presented value.
     * @return The last presented value.
     */
    long getLastValue();
}
