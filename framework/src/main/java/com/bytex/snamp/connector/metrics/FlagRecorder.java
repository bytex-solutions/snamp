package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.BooleanBinaryOperator;
import com.bytex.snamp.concurrent.TimeLimitedLong;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents implementation of {@link Flag}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class FlagRecorder extends AbstractMetric implements Flag {

    private final AtomicBoolean value;
    private final AtomicLong totalTrueCount;
    private final AtomicLong totalFalseCount;
    private final MetricsIntervalMap<TimeLimitedLong> lastTrueCount;
    private final MetricsIntervalMap<TimeLimitedLong> lastFalseCount;

    public FlagRecorder(final String name) {
        super(name);
        value = new AtomicBoolean(false);
        totalTrueCount = new AtomicLong(0L);
        totalFalseCount = new AtomicLong(0L);
        lastFalseCount = new MetricsIntervalMap<>(interval -> interval.createdAdder(0L));
        lastTrueCount = new MetricsIntervalMap<>(interval -> interval.createdAdder(0L));
    }

    protected void writeValue(final boolean value) {
        if(value){
            totalTrueCount.incrementAndGet();
            lastTrueCount.values().forEach(TimeLimitedLong::updateByOne);
        } else {
            totalFalseCount.incrementAndGet();
            lastFalseCount.values().forEach(TimeLimitedLong::updateByOne);
        }
    }

    /**
     * Updates this gauge with a new value.
     * @param value A new value to be placed into this gauge.
     */
    public final void accept(final boolean value){
        this.value.set(value);
        writeValue(value);
    }

    public final void accept(final boolean value, final BooleanBinaryOperator operator){
        boolean next, prev;
        do {
            next = operator.applyAsBoolean(prev = this.value.get(), value);
        } while (!this.value.compareAndSet(prev, next));
        writeValue(next);
    }

    /**
     * Inverses the value inside of this gauge.
     */
    public final void inverse() {
        accept(false, (current, provided) -> !current);
    }

    public final void or(final boolean value){
        accept(value, (current, provided) -> current | provided);
    }

    public final void and(final boolean value){
        accept(value, (current, provided) -> current & provided);
    }

    public final void xor(final boolean value){
        accept(value, (current, provided) -> current ^ provided);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        this.value.set(false);
        totalFalseCount.set(0L);
        totalTrueCount.set(0L);
        lastFalseCount.values().forEach(TimeLimitedLong::reset);
        lastTrueCount.values().forEach(TimeLimitedLong::reset);
    }

    /**
     * Gets number of submitted values.
     *
     * @param value Submitted value.
     * @return Number of submitted values.
     */
    @Override
    public final long getTotalCount(final boolean value) {
        return (value ? totalTrueCount : totalFalseCount).get();
    }

    /**
     * Gets ratio between true values and false values in historical perspective: count(true)/count(false)
     *
     * @return The ratio between true values and false values.
     */
    @Override
    public final double getTotalRatio() {
        final double trueCount = getTotalCount(true);
        final double falseCount = getTotalCount(false);
        return trueCount / falseCount;
    }

    /**
     * Gets ratio between true values and false values for the last time: count(true)/count(false)
     *
     * @param interval Measurement interval.
     * @return Ratio between true values and false values
     */
    @Override
    public final double getLastRatio(final MetricsInterval interval) {
        return (double) getLastCount(interval, true) / getLastCount(interval, false);
    }

    @Override
    public final long getLastCount(final MetricsInterval interval, final boolean value) {
        return (value ? lastTrueCount : lastFalseCount).get(interval).getAsLong();
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public final boolean getAsBoolean() {
        return this.value.get();
    }
}
