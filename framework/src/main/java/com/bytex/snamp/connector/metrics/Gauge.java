package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of the specified type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Gauge<V extends Comparable<V>> extends Metric {

    /**
     * Gets maximum value.
     * @return
     */
    V getMaxValue();

    V getMinValue();

    V getLastValue();
}
