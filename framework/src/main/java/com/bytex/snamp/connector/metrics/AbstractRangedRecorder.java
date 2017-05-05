package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;

import java.io.Serializable;
import java.time.Duration;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents abstract implementation of {@link Ranged}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractRangedRecorder extends AbstractMetric implements Ranged {
    private static final long serialVersionUID = 555166925381054756L;

    /**
     * Represents hit result when comparing some value with normative range.
     */
    protected enum HitResult implements Serializable{
        LESS_THAN_NORMATIVE,
        NORMAL,
        GREATER_THAN_NORMATIVE;

        public static HitResult compute(final long from, final long to, final long value){
            if(value < from)
                return LESS_THAN_NORMATIVE;
            else if(value > to)
                return GREATER_THAN_NORMATIVE;
            else
                return NORMAL;
        }

        public static HitResult compute(final double from, final double to, final double value){
            if(value < from)
                return LESS_THAN_NORMATIVE;
            else if(value > to)
                return GREATER_THAN_NORMATIVE;
            else
                return NORMAL;
        }

        public static HitResult compute(final Duration from, final Duration to, final Duration value){
            if(value.compareTo(from) < 0)
                return LESS_THAN_NORMATIVE;
            else if(value.compareTo(to) > 0)
                return GREATER_THAN_NORMATIVE;
            else
                return NORMAL;
        }
    }

    private final EnumMap<HitResult, AtomicLong> hits;
    private final EnumMap<HitResult, MetricsIntervalMap<TimeLimitedLong>> intervalHits;
    private final RateRecorder rate;

    protected AbstractRangedRecorder(final AbstractRangedRecorder source) {
        super(source);
        this.hits = new EnumMap<>(source.hits);
        intervalHits = new EnumMap<>(HitResult.class);
        for(final HitResult hr: HitResult.values()){
            final MetricsIntervalMap<TimeLimitedLong> counter = new MetricsIntervalMap<>(source.intervalHits.get(hr), TimeLimitedLong::clone);
            intervalHits.put(hr, counter);
        }
        this.rate = source.rate.clone();
    }

    protected AbstractRangedRecorder(final String name) {
        super(name);
        hits = new EnumMap<>(HitResult.class);
        intervalHits = new EnumMap<>(HitResult.class);
        for(final HitResult hr: HitResult.values()) {
            hits.put(hr, new AtomicLong(0L));
            intervalHits.put(hr, new MetricsIntervalMap<>(m -> m.createdAdder(0L)));
        }
        this.rate = new RateRecorder(name);
    }

    protected final void updateValue(final HitResult result) {
        hits.get(result).incrementAndGet();
        intervalHits.get(result).values().forEach(TimeLimitedLong::updateByOne);
        rate.mark();
    }

    @Override
    public abstract AbstractRangedRecorder clone();

    /**
     * Gets the total rate.
     *
     * @return The total rate.
     */
    @Override
    public final long getTotalRate() {
        return rate.getTotalRate();
    }

    /**
     * Gets the last measured rate of actions.
     *
     * @param interval Measurement interval.
     * @return The last measured rate of actions.
     */
    @Override
    public final long getLastRate(final MetricsInterval interval) {
        return rate.getLastRate(interval);
    }

    /**
     * Gets the mean rate of actions per unit of time from the historical perspective.
     *
     * @param scale Measurement interval.
     * @return Mean rate of actions per unit of time from the historical perspective.
     */
    @Override
    public final double getMeanRate(final MetricsInterval scale) {
        return rate.getMeanRate(scale);
    }

    /**
     * Gets the max rate of actions observed in the specified interval.
     *
     * @param interval Measurement interval.
     * @return The max rate of actions received in the specified interval.
     */
    @Override
    public final long getMaxRate(final MetricsInterval interval) {
        return rate.getMaxRate(interval);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePerSecond(final MetricsInterval interval) {
        return rate.getLastMaxRatePerSecond(interval);
    }

    /**
     * Gets the max rate of actions received per second for the last time.
     *
     * @param interval Measurement interval. Cannot be less than {@link MetricsInterval#MINUTE}.
     * @return The max rate of actions received per second for the last time.
     */
    @Override
    public final long getLastMaxRatePerMinute(final MetricsInterval interval) {
        return rate.getLastMaxRatePerMinute(interval);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        rate.mark();
        for(final HitResult hr: HitResult.values()){
            hits.get(hr).set(0L);
            intervalHits.get(hr).values().forEach(TimeLimitedLong::reset);
        }
    }

    private long getCountOfLessThanNormative() {
        return hits.get(HitResult.LESS_THAN_NORMATIVE).get();
    }

    /**
     * Gets percent of received measurements that are less than confidence interval.
     *
     * @return Percent of received measurements that are less than confidence interval.
     */
    @Override
    public final double getPercentOfLessThanRange() {
        final double totalCount = getTotalRate();
        return getCountOfLessThanNormative() / totalCount;
    }

    private long getCountOfLessThanNormative(final MetricsInterval interval) {
        return intervalHits.get(HitResult.LESS_THAN_NORMATIVE).getAsLong(interval, TimeLimitedLong::getAsLong);
    }

    /**
     * Gets percent of received measurements for the last time that are less than confidence interval.
     *
     * @param interval
     * @return Percent of received measurements for the last time that are less than confidence interval.
     */
    @Override
    public final double getPercentOfLessThanRange(final MetricsInterval interval) {
        final double totalCount = getLastRate(interval);
        return getCountOfLessThanNormative(interval) / totalCount;
    }

    private long getCountOfGreaterThanNormative() {
        return hits.get(HitResult.GREATER_THAN_NORMATIVE).get();
    }

    /**
     * Gets percent of received measurements that are greater than confidence interval.
     *
     * @return Percent of received measurements that are less than confidence interval.
     */
    @Override
    public final double getPercentOfGreaterThanRange() {
        final double totalCount = getTotalRate();
        return getCountOfGreaterThanNormative() / totalCount;
    }

    private long getCountOfGreaterThanNormative(final MetricsInterval interval) {
        return intervalHits.get(HitResult.GREATER_THAN_NORMATIVE).getAsLong(interval, TimeLimitedLong::getAsLong);
    }

    /**
     * Gets percent of received measurements for the last time that are greater than confidence interval.
     *
     * @param interval
     * @return Percent of received measurements for the last time that are less than confidence interval.
     */
    @Override
    public final double getPercentOfGreaterThanRange(final MetricsInterval interval) {
        final double totalCount = getLastRate(interval);
        return getCountOfGreaterThanNormative(interval) / totalCount;
    }

    private long getCountOfNormalValues() {
        return hits.get(HitResult.NORMAL).get();
    }

    /**
     * Gets percent of received measurements that are in normal range.
     *
     * @return Percent of received measurements that are in normal range.
     */
    @Override
    public final double getPercentOfValuesIsInRange() {
        final double totalCount = getTotalRate();
        return getCountOfNormalValues() / totalCount;
    }

    private long getCountOfNormalValues(final MetricsInterval interval) {
        return intervalHits.get(HitResult.NORMAL).getAsLong(interval, TimeLimitedLong::getAsLong);
    }

    /**
     * Gets percent of received measurements for the last time that are in normal range.
     *
     * @param interval
     * @return Percent of received measurements for the last time that are in normal range.
     */
    @Override
    public final double getPercentOfValuesIsInRange(final MetricsInterval interval) {
        final double totalCount = getLastRate(interval);
        return getCountOfNormalValues(interval) / totalCount;
    }
}
