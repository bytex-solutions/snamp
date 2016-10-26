package com.bytex.snamp.connector.metrics;

/**
 * Represents specialized version of floating-point gauge with double precision.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface GaugeFP extends NumericGauge {
    /**
     * Gets maximum value ever presented.
     * @return Maximum value ever presented.
     */
    double getMaxValue();

    /**
     * Gets maximum value for the last period.
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    double getLastMaxValue(final MetricsInterval interval);

    /**
     * The minimum value ever presented.
     * @return The minimum value ever presented.
     */
    double getMinValue();

    /**
     * Gets minimum value for the last period.
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    double getLastMinValue(final MetricsInterval interval);

    /**
     * The last presented value.
     * @return The last presented value.
     */
    double getLastValue();
}
