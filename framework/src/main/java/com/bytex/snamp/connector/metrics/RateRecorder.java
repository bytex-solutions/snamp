package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.bytex.snamp.connector.metrics.MetricsInterval.ALL_INTERVALS;

/**
 * Represents rate counter.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public class RateRecorder extends AbstractMetric implements Rate {
    private static final long serialVersionUID = -6735931494509416689L;
    private final MetricsIntervalMap<TimeLimitedLong> lastRate;
    private final MetricsIntervalMap<MeanRate> meanRate;
    private final MetricsIntervalMap<AtomicLong> maxRate;
    private final MetricsIntervalMap<TimeLimitedLong> lastMaxRatePerSecond;
    private final MetricsIntervalMap<TimeLimitedLong> lastMaxRatePerMinute;
    private final MetricsIntervalMap<TimeLimitedLong> lastMaxRatePer12Hours;
    private final AtomicLong totalRate;

    public RateRecorder(final String name){
        super(name);
        meanRate = new MetricsIntervalMap<>(MetricsInterval::createMeanRate);
        totalRate = new AtomicLong(0L);
        lastRate = new MetricsIntervalMap<>(MetricsInterval::createdAdder);
        maxRate = new MetricsIntervalMap<>(interval -> new AtomicLong(0L));
        lastMaxRatePerSecond = new MetricsIntervalMap<>(MetricsInterval.SECOND.greater(), MetricsInterval::createLongPeakDetector);
        lastMaxRatePerMinute = new MetricsIntervalMap<>(MetricsInterval.MINUTE.greater(), MetricsInterval::createLongPeakDetector);
        lastMaxRatePer12Hours = new MetricsIntervalMap<>(MetricsInterval.HALF_DAY.greater(), MetricsInterval::createLongPeakDetector);
    }

    protected RateRecorder(final RateRecorder source) {
        super(source);
        totalRate = new AtomicLong(source.totalRate.get());
        maxRate = new MetricsIntervalMap<>(source.maxRate, al -> new AtomicLong(al.get()));
        lastRate = new MetricsIntervalMap<>(source.lastRate, TimeLimitedLong::clone);
        lastMaxRatePerSecond = new MetricsIntervalMap<>(source.lastMaxRatePerSecond, TimeLimitedLong::clone);
        lastMaxRatePerMinute = new MetricsIntervalMap<>(source.lastMaxRatePerMinute, TimeLimitedLong::clone);
        lastMaxRatePer12Hours = new MetricsIntervalMap<>(source.lastMaxRatePer12Hours, TimeLimitedLong::clone);
        meanRate = new MetricsIntervalMap<>(source.meanRate, MeanRate::clone);
    }

    @Override
    public RateRecorder clone() {
        return new RateRecorder(this);
    }

    public void mark() {
        totalRate.incrementAndGet();
        for (final MetricsInterval interval : ALL_INTERVALS) {
            meanRate.get(interval).mark();
            final long lastRate = this.lastRate.getAsLong(interval, TimeLimitedLong::updateByOne);
            maxRate.acceptAsLong(interval, lastRate, (counter, lr) -> counter.accumulateAndGet(lr, Math::max));
            switch (interval){
                case SECOND: //write rate for the last second
                    lastMaxRatePerSecond.forEachAcceptLong(lastRate, TimeLimitedLong::accept);
                    break;
                case MINUTE: //write rate for the last minute
                    lastMaxRatePerMinute.forEachAcceptLong(lastRate, TimeLimitedLong::accept);
                case HALF_DAY:   //write rate for the last 12 hours
                    lastMaxRatePer12Hours.forEachAcceptLong(lastRate, TimeLimitedLong::accept);
            }
        }
    }

    private static Instant minInstant(final Instant current, final Instant provided){
        return current.compareTo(provided) < 0 ? current : provided;
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
        return meanRate.get(scale).getAsDouble();
    }

    @Override
    public final long getMaxRate(final MetricsInterval interval) {
        return maxRate.getAsLong(interval, AtomicLong::get);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePerSecond(final MetricsInterval interval) {
        return lastMaxRatePerSecond.containsKey(interval) ?
                lastMaxRatePerSecond.getAsLong(interval, TimeLimitedLong::getAsLong) :
                getLastRate(interval);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval. Cannot be less than {@link MetricsInterval#MINUTE}.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePerMinute(final MetricsInterval interval) {
        return lastMaxRatePerMinute.containsKey(interval) ?
                lastMaxRatePerMinute.getAsLong(interval, TimeLimitedLong::getAsLong) :
                getLastRate(interval);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval. Cannot be less than {@link MetricsInterval#HALF_DAY}.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePer12Hours(final MetricsInterval interval) {
        return lastMaxRatePer12Hours.containsKey(interval) ?
                lastMaxRatePer12Hours.getAsLong(interval, TimeLimitedLong::getAsLong) :
                getLastRate(interval);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        totalRate.set(0L);
        lastRate.values().forEach(TimeLimitedLong::reset);
        maxRate.values().forEach(rate -> rate.set(0L));
        meanRate.values().forEach(MeanRate::reset);
        lastMaxRatePerSecond.values().forEach(TimeLimitedLong::reset);
        lastMaxRatePerMinute.values().forEach(TimeLimitedLong::reset);
        lastMaxRatePer12Hours.values().forEach(TimeLimitedLong::reset);
    }
}
