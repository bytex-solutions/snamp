package com.bytex.snamp.connector.metrics;

import java.time.Duration;

/**
 * Measures timing of some event.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Timing extends Gauge<Duration>, Statistic {
    /**
     * Gets quantile of durations, in seconds.
     * @param quantile Quantile value.
     * @return
     */
    @Override
    double getQuantile(final double quantile);

    @Override
    double getDeviation();

    default double getMeanNumberOfCompletedTasks(final MetricsInterval interval) {
        return 1D / interval.divideFP(getMean());
    }

    default double getMaxNumberOfCompletedTasks(final MetricsInterval interval){
        return 1D / interval.divideFP(getMinDuration());
    }

    default double getMinNumberOfCompletedTasks(final MetricsInterval interval){
        return 1D / interval.divideFP(getMaxDuration());
    }

    /**
     * Gets summary duration of all events.
     * @return The summary duration of all events.
     */
    Duration getSummaryValue();
}
