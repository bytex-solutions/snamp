package com.bytex.snamp.connector.metrics;

import java.time.Duration;

/**
 * Measures timing of actions.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Timer extends Gauge<Duration>, Metric {

    /**
     * Gets duration at the specified quantile.
     * @param quantile The quantile value.
     * @return Duration at the specified quantile.
     */
    Duration getQuantile(final double quantile);

    /**
     * Gets standard deviation of all durations.
     * @return The standard deviation of all durations.
     */
    Duration getDeviation();

    double getMeanNumberOfCompletedTasks(final MetricsInterval interval);

    double getMaxNumberOfCompletedTasks(final MetricsInterval interval);

    double getMinNumberOfCompletedTasks(final MetricsInterval interval);

    /**
     * Gets summary duration of all events.
     * @return The summary duration of all events.
     */
    Duration getSummaryValue();

    Duration getLastMeanValue(final MetricsInterval interval);

    Duration getMeanValue();

    @Override
    Timer clone();
}
