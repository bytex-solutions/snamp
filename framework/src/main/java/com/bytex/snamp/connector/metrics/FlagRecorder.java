package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;
import com.bytex.snamp.math.ExponentialMovingAverage;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents implementation of {@link Flag}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FlagRecorder extends AbstractMetric implements Flag {
    private interface BooleanBinaryOperator{
        boolean applyAsBoolean(final boolean current, final boolean provided);
    }

    private final AtomicBoolean value;
    private final AtomicLong totalTrueCount;
    private final AtomicLong totalFalseCount;
    private final EnumMap<MetricsInterval, ExponentialMovingAverage> meanRatio;
    private final EnumMap<MetricsInterval, TimeLimitedLong> lastTrueCount;
    private final EnumMap<MetricsInterval, TimeLimitedLong> lastFalseCount;

    public FlagRecorder(final String name) {
        super(name);
        value = new AtomicBoolean(false);
        totalTrueCount = new AtomicLong(0L);
        totalFalseCount = new AtomicLong(0L);
        meanRatio = new EnumMap<>(MetricsInterval.class);
        lastFalseCount = new EnumMap<>(MetricsInterval.class);
        lastTrueCount = new EnumMap<>(MetricsInterval.class);
        for(final MetricsInterval interval: MetricsInterval.values()){
            meanRatio.put(interval, interval.createEMA());
            lastFalseCount.put(interval, interval.createdAdder(0L));
            lastTrueCount.put(interval, interval.createdAdder(0L));
        }
    }

    private void updateMeanRatio(final double trueCount, final double falseCount, final MetricsInterval interval) {
        if (falseCount > 0D) //avoid divide by zero error
            meanRatio.get(interval).accept(trueCount / falseCount);
    }

    private void updateRatio(final boolean value) {
        if (value) {
            final double trueCount = totalTrueCount.incrementAndGet();
            final double falseCount = totalFalseCount.get();
            for (final MetricsInterval interval : MetricsInterval.values()) {
                updateMeanRatio(trueCount, falseCount, interval);
                lastTrueCount.get(interval).update(1L);
            }
        } else {
            final double falseCount = totalFalseCount.incrementAndGet();
            final double trueCount = totalTrueCount.get();
            for (final MetricsInterval interval : MetricsInterval.values()) {
                updateMeanRatio(trueCount, falseCount, interval);
                lastFalseCount.get(interval).update(1L);
            }
        }
    }

    /**
     * Updates this gauge with a new value.
     * @param value A new value to be placed into this gauge.
     */
    public void update(final boolean value){
        this.value.set(value);
        updateRatio(value);
    }

    private void update(final boolean value, final BooleanBinaryOperator operator){
        boolean next, prev;
        do {
            next = operator.applyAsBoolean(prev = this.value.get(), value);
        } while (!this.value.compareAndSet(prev, next));
        updateRatio(next);
    }

    /**
     * Inverses the value inside of this gauge.
     */
    public void inverse() {
        update(false, (current, provided) -> !current);
    }

    public void or(final boolean value){
        update(value, (current, provided) -> current | provided);
    }

    public void and(final boolean value){
        update(value, (current, provided) -> current & provided);
    }

    public void xor(final boolean value){
        update(value, (current, provided) -> current ^ provided);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        this.value.set(false);
        totalFalseCount.set(0L);
        totalTrueCount.set(0L);
    }

    /**
     * Gets ratio between true values and false values in historical perspective: count(true)/count(false)
     *
     * @return The ratio between true values and false values.
     */
    @Override
    public double getTotalRatio() {
        return (double) totalTrueCount.get() / totalFalseCount.get();
    }

    /**
     * Gets ratio between true values and false values for the last time: count(true)/count(false)
     *
     * @param interval Measurement interval.
     * @return Ratio between true values and false values
     */
    @Override
    public double getLastRatio(final MetricsInterval interval) {
        return (double) lastTrueCount.get(interval).getAsLong() / lastFalseCount.get(interval).getAsLong();
    }

    /**
     * Gets mean ratio between true values and false values for the period of time: count(true)/count(false)
     *
     * @param interval Measurement interval.
     * @return Ratio between true values and false values.
     */
    @Override
    public double getMeanRatio(final MetricsInterval interval) {
        return meanRatio.get(interval).getAsDouble();
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public boolean getAsBoolean() {
        return this.value.get();
    }
}
