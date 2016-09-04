package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;
import com.bytex.snamp.math.ExponentialMovingAverage;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents rate counter.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class RateRecorder extends AbstractMetric implements Rate {
    private final EnumMap<MetricsInterval, TimeLimitedLong> lastRate;
    private final EnumMap<MetricsInterval, TimeLimitedLong> maxRateInInterval;
    private final EnumMap<MetricsInterval, ExponentialMovingAverage> meanRate;
    private final AtomicLong maxRate;
    private final AtomicLong totalRate;
    private final AtomicReference<Instant> startTime;

    public RateRecorder(final String name){
        super(name);
        totalRate = new AtomicLong(0L);
        maxRate = new AtomicLong(0L);
        lastRate = new EnumMap<>(MetricsInterval.class);
        maxRateInInterval = new EnumMap<>(MetricsInterval.class);
        meanRate = new EnumMap<>(MetricsInterval.class);
        for(final MetricsInterval interval: MetricsInterval.values()){
            lastRate.put(interval, interval.createdAdder(0L));
            maxRateInInterval.put(interval, interval.createPeakCounter(Long.MIN_VALUE));
            meanRate.put(interval, interval.createEMA());
        }
        startTime = new AtomicReference<>(Instant.now());
    }

    public void update(){
        totalRate.incrementAndGet();
        for(final MetricsInterval interval: MetricsInterval.values()){
            final long lastRate = this.lastRate.get(interval).update(1L);
            maxRateInInterval.get(interval).update(lastRate);
            maxRate.accumulateAndGet(lastRate, Math::max);
            meanRate.get(interval).accept(1D);
        }
    }

    private static Instant minInstant(final Instant current, final Instant provided){
        return current.compareTo(provided) < 0 ? current : provided;
    }

    public void setStartTime(final Instant value) {
        startTime.accumulateAndGet(value, RateRecorder::minInstant);
    }

    /**
     * Gets the total rate.
     *
     * @return The total rate.
     */
    @Override
    public long getTotalRate() {
        return totalRate.get();
    }

    /**
     * Gets the last measured rate of actions.
     *
     * @param interval Measurement interval.
     * @return The last measured rate of actions.
     */
    @Override
    public long getLastRate(final MetricsInterval interval) {
        return lastRate.get(interval).getAsLong();
    }

    /**
     * Gets the mean rate of actions received for the last time.
     *
     * @param interval Measurement interval.
     * @return The mean rate of actions received for the last time.
     */
    @Override
    public double getLastMeanRate(final MetricsInterval interval) {
        final Duration timeline = Duration.between(startTime.get(), Instant.now());
        return getTotalRate() / interval.divideFP(timeline);
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

    @Override
    public long getLastMaxRate(final MetricsInterval interval) {
        return Math.max(0L, maxRateInInterval.get(interval).getAsLong());
    }

    /**
     * Gets the maximum rate ever happened.
     *
     * @return The maximum rate ever happened.
     */
    @Override
    public long getMaxRate() {
        return maxRate.get();
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        maxRate.set(0L);
        totalRate.set(0L);
        for(final MetricsInterval interval: MetricsInterval.values()){
            lastRate.get(interval).reset();
            meanRate.get(interval).reset();
            maxRateInInterval.get(interval).reset();
        }
    }
}
