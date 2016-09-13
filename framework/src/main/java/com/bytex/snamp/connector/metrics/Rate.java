package com.bytex.snamp.connector.metrics;

/**
 * Provides rate of actions.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Rate extends Metric {
    /**
     * Gets the total rate.
     * @return The total rate.
     */
    long getTotalRate();

    /**
     * Gets the last measured rate of actions.
     *
     * @param interval Measurement interval.
     * @return The last measured rate of actions.
     */
    long getLastRate(final MetricsInterval interval);

    /**
     * Gets the mean rate of actions received for the last time.
     * @param interval Measurement interval.
     * @return The mean rate of actions received for the last time.
     */
    double getLastMeanRate(final MetricsInterval interval);

    /**
     * Gets the mean rate of actions per unit of time from the historical perspective.
     * @param scale Measurement interval.
     * @return Mean rate of actions per unit of time from the historical perspective.
     */
    double getMeanRate(final MetricsInterval scale);

    /**
     * Gets the max rate of actions received for the last time.
     * @param interval Measurement interval.
     * @return The max rate of actions received for the last time.
     */
    long getMaxRate(final MetricsInterval interval);
}
