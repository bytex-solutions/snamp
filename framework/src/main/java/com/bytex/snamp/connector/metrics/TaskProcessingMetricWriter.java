package com.bytex.snamp.connector.metrics;


import com.bytex.snamp.concurrent.LongAccumulator;
import com.bytex.snamp.math.ExponentiallyMovingAverage;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents implementation of {@link Timing} and {@link Rate}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TaskProcessingMetricWriter extends AbstractMetric implements Timing, Rate {
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
    private final EnumMap<MetricsInterval, LongAccumulator> lastRate;
    private final EnumMap<MetricsInterval, LongAccumulator> maxRateInInterval;
    private final EnumMap<MetricsInterval, ExponentiallyMovingAverage> meanRate;
    private final AtomicLong maxRate;
    private final AtomicReference<Instant> startTime;

    public TaskProcessingMetricWriter(final String name) {
        super(name);
        maxTiming = new AtomicReference<>(Duration.ZERO);
        minTiming = new AtomicReference<>(null);
        summary = new AtomicReference<>(new TimingAndRate());
        startTime = new AtomicReference<>(Instant.now());
        lastTiming = new AtomicReference<>(Duration.ZERO);
        lastRate = new EnumMap<>(MetricsInterval.class);
        maxRateInInterval = new EnumMap<>(MetricsInterval.class);
        maxRate = new AtomicLong(0L);
        meanRate = new EnumMap<>(MetricsInterval.class);
        for(final MetricsInterval interval: MetricsInterval.values()){
            lastRate.put(interval, interval.createdAdder(0L));
            maxRateInInterval.put(interval, interval.createPeakCounter(0L));
            meanRate.put(interval, interval.createEMA());
        }
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

    private static Instant minInstant(final Instant current, final Instant provided){
        return current.compareTo(provided) < 0 ? current : provided;
    }

    public void update(final Duration eventDuration) {
        maxTiming.accumulateAndGet(eventDuration, TaskProcessingMetricWriter::maxDuration);
        minTiming.accumulateAndGet(eventDuration, TaskProcessingMetricWriter::minDuration);
        summary.accumulateAndGet(new TimingAndRate(eventDuration, 1L), TimingAndRate::plus);
        lastTiming.set(eventDuration);
        for(final MetricsInterval interval: MetricsInterval.values()){
            final long lastRate = this.lastRate.get(interval).update(1L);
            maxRateInInterval.get(interval).update(lastRate);
            maxRate.accumulateAndGet(lastRate, Math::max);
            meanRate.get(interval).accept(1D);
        }
    }

    /**
     * Gets the mean rate of actions per unit time from the historical perspective.
     *
     * @param interval Measurement interval.
     * @return Mean rate of actions per unit time from the historical perspective.
     */
    @Override
    public double getMeanRate(final MetricsInterval interval) {
        return meanRate.get(interval).getAsDouble();
    }

    public void update(final Instant eventStart, final Instant eventEnd) {
        setStartTime(eventStart);
        update(Duration.between(eventStart, eventEnd));
    }

    public void setStartTime(final Instant value){
        startTime.accumulateAndGet(value, TaskProcessingMetricWriter::minInstant);
    }

    public long getMaxRate() {
        return maxRate.get();
    }

    @Override
    public long getTotalRate() {
        return summary.get().count;
    }

    @Override
    public long getLastRate(final MetricsInterval interval) {
        return lastRate.get(interval).getAsLong();
    }

    @Override
    public double getLastMeanRate(final MetricsInterval interval) {
        final Duration timeline = Duration.between(startTime.get(), Instant.now());
        return getTotalRate() / interval.divideFP(timeline);
    }

    @Override
    public long getLastMaxRate(final MetricsInterval interval) {
        return maxRateInInterval.get(interval).getAsLong();
    }

    @Override
    public Duration getMinDuration() {
        return firstNonNull(minTiming.get(), Duration.ZERO);
    }

    @Override
    public Duration getMaxDuration() {
        return maxTiming.get();
    }

    @Override
    public Duration getMeanDuration() {
        return summary.get().divide();
    }

    @Override
    public Duration getLastDuration() {
        return lastTiming.get();
    }

    @Override
    public Duration getSummaryDuration() {
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
        maxRate.set(0L);
        for(final MetricsInterval interval: MetricsInterval.values()){
            lastRate.get(interval).reset();
            maxRateInInterval.get(interval).reset();
            meanRate.get(interval).reset();
        }
    }
}
