package com.bytex.snamp.connector.metrics;

/**
 * Provides measurement of events rate.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Rate extends Metric {
    long getTotalCount();

    /**
     * Gets the last measured count of events per unit of time.
     *
     * @param interval Unit of time.
     * @return The last measured count of events.
     */
    long getLastCount(final MetricsInterval interval);

    double getAverageCount(final MetricsInterval interval);

    long getMaxCount(final MetricsInterval interval);

    long getMinCount(final MetricsInterval interval);
}
