package com.bytex.snamp.connector.metrics;


import com.bytex.snamp.concurrent.LongAccumulator;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents implementation of {@link Timing} and {@link Rate}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TimingWriter implements Timing, Rate {
    //tuple with duration and rate
    private static final class TimingAndRate{
        private final Duration timing;
        private final long count;

        private TimingAndRate(){
            this(Duration.ZERO, 0);
        }

        private TimingAndRate(final Duration t, final long count){
            this.timing = t;
            this.count = count;
        }

        private TimingAndRate plus(final Duration t, final long count){
            return new TimingAndRate(this.timing.plus(t), this.count + count);
        }

        private TimingAndRate plus(final TimingAndRate other){
            return plus(other.timing, other.count);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timing, count);
        }

        private boolean equals(final TimingAndRate other){
            return timing.equals(other.timing) && count == other.count;
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof TimingAndRate && equals((TimingAndRate)other);
        }

        private Duration divide() {
            return count == 0L ? timing : timing.dividedBy(count);
        }
    }

    private final AtomicReference<Duration> maxTiming;
    private final AtomicReference<Duration> minTiming;
    private final AtomicReference<TimingAndRate> summary;
    private final AtomicReference<Duration> lastTiming;
    private final EnumMap<MetricsInterval, LongAccumulator> lastCount;

    public TimingWriter() {
        maxTiming = new AtomicReference<>(Duration.ZERO);
        minTiming = new AtomicReference<>(null);
        summary = new AtomicReference<>(new TimingAndRate());
        lastTiming = new AtomicReference<>(Duration.ZERO);
        lastCount = new EnumMap<>(MetricsInterval.class);
    }

    private static Duration maxDuration(final Duration current, final Duration provided){
        return current.compareTo(provided) > 0 ? current : provided;
    }

    private static Duration minDuration(final Duration current, final Duration provider){
        if(current == null)
            return provider;
        else
            return current.compareTo(provider) < 0 ? current : provider;
    }

    public void update(final Duration eventDuration) {
        maxTiming.accumulateAndGet(eventDuration, TimingWriter::maxDuration);
        minTiming.accumulateAndGet(eventDuration, TimingWriter::minDuration);
        summary.accumulateAndGet(new TimingAndRate(eventDuration, 1L), TimingAndRate::plus);
        lastTiming.set(eventDuration);
    }

    @Override
    public long getTotalCount() {
        return summary.get().count;
    }

    /**
     * Gets the last measured count of events per unit of time.
     *
     * @param interval Unit of time.
     * @return The last measured count of events.
     */
    @Override
    public long getLastCount(final MetricsInterval interval) {
        return 0;
    }

    @Override
    public double getAverageCount(final MetricsInterval interval) {
        return 0;
    }

    @Override
    public long getMaxCount(final MetricsInterval interval) {
        return 0;
    }

    @Override
    public long getMinCount(final MetricsInterval interval) {
        return 0;
    }

    /**
     * Gets minimal timing.
     *
     * @return The minimal timing.
     */
    @Override
    public Duration getMinimum() {
        return firstNonNull(minTiming.get(), Duration.ZERO);
    }

    /**
     * Gets maximal timing.
     *
     * @return The maximal timing.
     */
    @Override
    public Duration getMaximum() {
        return maxTiming.get();
    }

    /**
     * Gets average timing.
     *
     * @return The average timing.
     */
    @Override
    public Duration getAverage() {
        return summary.get().divide();
    }

    /**
     * Gets the last timing.
     *
     * @return The last timing.
     */
    @Override
    public Duration getLast() {
        return lastTiming.get();
    }

    /**
     * Gets summary duration of all events.
     *
     * @return The summary duration of all events.
     */
    @Override
    public Duration getSummary() {
        return summary.get().timing;
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        minTiming.set(null);
        maxTiming.set(Duration.ZERO);
        summary.set(new TimingAndRate());
        lastTiming.set(Duration.ZERO);
    }
}
