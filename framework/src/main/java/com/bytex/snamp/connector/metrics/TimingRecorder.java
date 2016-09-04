package com.bytex.snamp.connector.metrics;


import java.time.Duration;

/**
 * Represents implementation of {@link Timing}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TimingRecorder extends GaugeImpl<Duration> implements Timing {

    public TimingRecorder(final String name) {
        super(name, Duration.ZERO);
    }

    /**
     * Gets duration at the specified quantile.
     *
     * @param quantile The quantile value.
     * @return Duration at the specified quantile.
     */
    @Override
    public Duration getQuantile(final double quantile) {
        return null;
    }

    /**
     * Gets standard deviation of all durations.
     *
     * @return The standard deviation of all durations.
     */
    @Override
    public Duration getDeviation() {
        return null;
    }

    /**
     * Gets summary duration of all events.
     *
     * @return The summary duration of all events.
     */
    @Override
    public Duration getSummaryValue() {
        return null;
    }
}
