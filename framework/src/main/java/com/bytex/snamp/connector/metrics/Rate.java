package com.bytex.snamp.connector.metrics;

/**
 * Provides measurement of events rate.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Rate extends Metric {
    long getTotalRate();

    /**
     * Gets the last measured count of events per unit of time.
     *
     * @param interval Unit of time.
     * @return The last measured count of events.
     */
    long getLastRate(final MetricsInterval interval);

    double getMeanRate(final MetricsInterval interval);

    long getMaxRate(final MetricsInterval interval);
}
