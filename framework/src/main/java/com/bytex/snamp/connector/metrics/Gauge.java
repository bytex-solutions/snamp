package com.bytex.snamp.connector.metrics;

import java.util.function.Consumer;

/**
 * Represents generic gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface Gauge<V extends Comparable<V>> extends Metric, Consumer<V> {

    /**
     * Gets maximum value ever presented.
     * @return Maximum value ever presented.
     */
    V getMaxValue();

    /**
     * Gets maximum value for the last period of time.
     * @param interval Period of time.
     * @return Maximum value of the last period of time.
     */
    V getLastMaxValue(final MetricsInterval interval);

    /**
     * The minimum value ever presented.
     * @return The minimum value ever presented.
     */
    V getMinValue();

    /**
     * Gets minimum value for the last period of time.
     * @param interval Period of time.
     * @return Minimum value for the last period of time.
     */
    V getLastMinValue(final MetricsInterval interval);

    /**
     * The last presented value.
     * @return The last presented value.
     */
    V getLastValue();
}
