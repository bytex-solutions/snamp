package com.bytex.snamp.connector.metrics;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents abstract class for all metrics.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class AbstractMetric implements Metric, Serializable {
    private static final long serialVersionUID = -8381259894349243894L;
    private final String name;

    protected AbstractMetric(final AbstractMetric source){
        this.name = source.name;
    }

    protected AbstractMetric(final String name){
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public abstract AbstractMetric clone();

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
