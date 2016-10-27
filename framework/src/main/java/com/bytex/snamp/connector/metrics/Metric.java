package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.Stateful;

/**
 * Represents a root interface for different type of metric sets.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface Metric extends Stateful, Cloneable {
    /**
     * Gets name of this metric.
     * @return Name of this metric.
     */
    String getName();

    /**
     * Resets all metrics.
     */
    @Override
    void reset();

    Metric clone();
}
