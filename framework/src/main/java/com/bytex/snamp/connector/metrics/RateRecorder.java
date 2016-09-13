package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;
import com.bytex.snamp.math.ExponentialMovingAverage;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import static com.bytex.snamp.connector.metrics.MetricsInterval.ALL_INTERVALS;

/**
 * Represents rate counter.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class RateRecorder extends AbstractMetric implements Rate {
    private final MetricsIntervalMap<TimeLimitedLong> lastRate;
    private final MetricsIntervalMap<AtomicLong> maxRate;
    private final MetricsIntervalMap<ExponentialMovingAverage> meanRate;
    private final AtomicLong totalRate;
    private final AtomicReference<Instant> startTime;

    public RateRecorder(final String name){
        super(name);
        totalRate = new AtomicLong(0L);
        lastRate = new MetricsIntervalMap<>(interval -> interval.createdAdder(0L));
        maxRate = new MetricsIntervalMap<>(interval -> new AtomicLong(0L));
        meanRate = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        startTime = new AtomicReference<>(Instant.now());
    }

    public void mark() {
        totalRate.incrementAndGet();
        for (final MetricsInterval interval : ALL_INTERVALS) {
            final long lastRate = this.lastRate.getAsLong(interval, TimeLimitedLong::updateByOne);
            maxRate.acceptAsLong(interval, lastRate, (counter, lr) -> counter.accumulateAndGet(lr, Math::max));
            meanRate.acceptAsDouble(interval, 1D, ExponentialMovingAverage::accept);
        }
    }

    private static Instant minInstant(final Instant current, final Instant provided){
        return current.compareTo(provided) < 0 ? current : provided;
    }

    final Instant getStartTime(){
        return startTime.get();
    }

    public final void setStartTime(final Instant value) {
        startTime.accumulateAndGet(value, RateRecorder::minInstant);
    }

    /**
     * Gets the total rate.
     *
     * @return The total rate.
     */
    @Override
    public final long getTotalRate() {
        return totalRate.get();
    }

    /**
     * Gets the last measured rate of actions.
     *
     * @param interval Measurement interval.
     * @return The last measured rate of actions.
     */
    @Override
    public final long getLastRate(final MetricsInterval interval) {
        return lastRate.getAsLong(interval, TimeLimitedLong::getAsLong);
    }

    /**
     * Gets the mean rate of actions per unit time from the historical perspective.
     *
     * @param scale Measurement interval.
     * @return Mean rate of actions per unit time from the historical perspective.
     */
    @Override
    public final double getMeanRate(final MetricsInterval scale) {
        final Duration timeline = Duration.between(startTime.get(), Instant.now());
        return getTotalRate() / scale.divideFP(timeline);
    }

    /**
     * Gets the mean rate of actions received for the last time.
     *
     * @param interval Measurement interval.
     * @return The mean rate of actions received for the last time.
     */
    @Override
    public double getLastMeanRate(final MetricsInterval interval) {
        return meanRate.getAsDouble(interval, ExponentialMovingAverage::getAsDouble);
    }

    @Override
    public final long getMaxRate(final MetricsInterval interval) {
        return maxRate.getAsLong(interval, AtomicLong::get);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        totalRate.set(0L);
        lastRate.values().forEach(TimeLimitedLong::reset);
        meanRate.values().forEach(ExponentialMovingAverage::reset);
        maxRate.values().forEach(rate -> rate.set(0L));
    }
}
