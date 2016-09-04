package com.bytex.snamp.connector.metrics;

import java.time.Duration;

/**
 * Measures timing of actions.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Timing extends Gauge<Duration>, Metric {

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

    default double getMeanNumberOfCompletedTasks(final MetricsInterval interval) {
        return 1D / interval.divideFP(getQuantile(0.5));
    }

    default double getMaxNumberOfCompletedTasks(final MetricsInterval interval){
        return 1D / interval.divideFP(getMinValue());
    }

    default double getMinNumberOfCompletedTasks(final MetricsInterval interval){
        return 1D / interval.divideFP(getMaxValue());
    }

    /**
     * Gets summary duration of all events.
     * @return The summary duration of all events.
     */
    Duration getSummaryValue();
}
