package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of {@link String} type.
 * @author Roman Sakno
 * @version 2.1
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
     * Gets maximum value for the last period.
     *
     * @param interval Period.
     * @return Maximum value of the last period.
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
     * Gets minimum value for the last period.
     *
     * @param interval Period.
     * @return Minimum value for the last period.
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

    @Override
    StringGauge clone();
}
