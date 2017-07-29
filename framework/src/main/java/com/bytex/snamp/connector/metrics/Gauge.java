package com.bytex.snamp.connector.metrics;

/**
 * Represents generic gauge.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
interface Gauge<V extends Comparable<V>> extends Metric {

    /**
     * Gets maximum value ever presented.
     * @return Maximum value ever presented.
     */
    V getMaxValue();

    /**
     * Gets maximum value for the last period.
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    V getLastMaxValue(final MetricsInterval interval);

    /**
     * The minimum value ever presented.
     * @return The minimum value ever presented.
     */
    V getMinValue();

    /**
     * Gets minimum value for the last period.
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    V getLastMinValue(final MetricsInterval interval);

    /**
     * The last presented value.
     * @return The last presented value.
     */
    V getLastValue();

    @Override
    Gauge<V> clone();
}
