package com.bytex.snamp.connector.metrics;

import java.util.function.BooleanSupplier;

/**
 * Represents flag.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Flag extends Metric, BooleanSupplier {
    /**
     * Gets number of submitted values.
     * @param value Submitted value.
     * @return Number of submitted values.
     */
    long getTotalCount(final boolean value);

    long getLastCount(final MetricsInterval interval, final boolean value);

    /**
     * Gets ratio between true values and false values in historical perspective: count(true)/count(false)
     * @return The ratio between true values and false values.
     */
    double getTotalRatio();

    /**
     * Gets ratio between true values and false values for the last time: count(true)/count(false)
     * @param interval Measurement interval.
     * @return Ratio between true values and false values
     */
    double getLastRatio(final MetricsInterval interval);

    @Override
    Flag clone();
}
