package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of {@link String} type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface StringGauge extends Gauge<String> {
    /**
     * Gets maximum value ever presented.
     *
     * @return Maximum value ever presented.
     */
    @Override
    String getMaxValue();

    /**
     * Gets maximum value for the last period of time.
     *
     * @param interval Period of time.
     * @return Maximum value of the last period of time.
     */
    @Override
    String getLastMaxValue(final MetricsInterval interval);

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    String getMinValue();

    /**
     * Gets minimum value for the last period of time.
     *
     * @param interval Period of time.
     * @return Minimum value for the last period of time.
     */
    @Override
    String getLastMinValue(final MetricsInterval interval);

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    String getLastValue();
}
