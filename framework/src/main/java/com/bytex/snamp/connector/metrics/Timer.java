package com.bytex.snamp.connector.metrics;

import java.time.Duration;

/**
 * Measures timing of actions.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Timer extends Gauge<Duration> {

    /**
     * Gets duration at the specified quantile.
     * @param quantile The quantile value.
     * @return Duration at the specified quantile.
     */
    Duration getQuantile(final float quantile);

    /**
     * Gets standard deviation of all durations.
     * @return The standard deviation of all durations.
     */
    Duration getDeviation();

    @Override
    Duration getMaxValue();

    @Override
    Duration getLastMaxValue(final MetricsInterval interval);

    @Override
    Duration getMinValue();

    @Override
    Duration getLastMinValue(final MetricsInterval interval);

    @Override
    Duration getLastValue();

    double getMeanNumberOfCompletedTasks(final MetricsInterval scale);

    double getMaxNumberOfCompletedTasks(final MetricsInterval scale);

    double getMinNumberOfCompletedTasks(final MetricsInterval scale);

    /**
     * Gets summary duration of all events.
     * @return The summary duration of all events.
     */
    Duration getSummaryValue();

    Duration getLastMeanValue(final MetricsInterval interval);

    Duration getMeanValue();

    /**
     * Gets summary duration of all events for the last time.
     * @param interval Interval of measurement.
     * @return Summary duration of all events for the last time.
     */
    Duration getSummaryValue(final MetricsInterval interval);

    @Override
    Timer clone();
}
