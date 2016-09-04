package com.bytex.snamp.connector.metrics;

/**
 * Represents generic gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface Gauge<V extends Comparable<V>> extends Metric {

    /**
     * Gets maximum value ever presented.
     * @return The maximum value ever presented.
     */
    V getMaxValue();

    /**
     * The minimum value ever presented.
     * @return The minimum value ever presented.
     */
    V getMinValue();

    /**
     * The last presented value.
     * @return The last presented value.
     */
    V getLastValue();
}
