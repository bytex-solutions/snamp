package com.bytex.snamp.connector.metrics;

import java.util.Objects;

/**
 * Represents abstract class for all metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractMetric implements Metric {
    private final String name;

    AbstractMetric(final String name){
        this.name = Objects.requireNonNull(name);
    }

    /**
     * Gets name of this metric.
     *
     * @return Name of this metric.
     */
    @Override
    public final String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
