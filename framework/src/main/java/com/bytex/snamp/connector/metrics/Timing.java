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
     * Gets minimal timing.
     * @return The minimal timing.
     */
    Duration getMinimum();

    /**
     * Gets maximal timing.
     * @return The maximal timing.
     */
    Duration getMaximum();

    /**
     * Gets average timing.
     * @return The average timing.
     */
    Duration getAverage();

    /**
     * Gets the last timing.
     * @return The last timing.
     */
    Duration getLast();

    /**
     * Gets summary duration of all events.
     * @return The summary duration of all events.
     */
    Duration getSummary();
}
