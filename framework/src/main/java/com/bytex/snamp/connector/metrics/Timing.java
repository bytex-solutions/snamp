package com.bytex.snamp.connector.metrics;

import java.time.Duration;

/**
 * Measures timing of some event.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Timing extends Metric {
    /**
     * Gets minimum duration of task processing.
     * @return The minimum duration of task processing.
     */
    Duration getMinDuration();

    /**
     * Gets maximal timing.
     * @return The maximal timing.
     */
    Duration getMaxDuration();

    /**
     * Gets average timing.
     * @return The average timing.
     */
    Duration getMeanDuration();

    default double getMeanNumberOfCompletedTasks(final MetricsInterval interval) {
        return 1D / interval.divideFP(getMeanDuration());
    }

    default double getMaxNumberOfCompletedTasks(final MetricsInterval interval){
        return 1D / interval.divideFP(getMinDuration());
    }

    default double getMinNumberOfCompletedTasks(final MetricsInterval interval){
        return 1D / interval.divideFP(getMaxDuration());
    }

    /**
     * Gets the last timing.
     * @return The last timing.
     */
    Duration getLastDuration();

    /**
     * Gets summary duration of all events.
     * @return The summary duration of all events.
     */
    Duration getSummaryDuration();
}
