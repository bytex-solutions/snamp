package com.bytex.snamp.connector.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

/**
 * Represents implementation of {@link Gauge64}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class Gauge64Recorder extends AbstractNumericGauge implements Gauge64, LongConsumer {
    private static final long serialVersionUID = 7528670309055651559L;
    private final AtomicLong maxValue;
    private final AtomicLong minValue;
    private final AtomicLong lastValue;

    public Gauge64Recorder(final String name, final int samplingSize) {
        super(name, samplingSize);
        maxValue = new AtomicLong(Long.MIN_VALUE);
        minValue = new AtomicLong(Long.MAX_VALUE);
        lastValue = new AtomicLong(0L);
    }

    public Gauge64Recorder(final String name){
        this(name, DEFAULT_SAMPLING_SIZE);
    }

    protected Gauge64Recorder(final Gauge64Recorder source){
        super(source);
        maxValue = new AtomicLong(source.maxValue.get());
        minValue = new AtomicLong(source.minValue.get());
        lastValue = new AtomicLong(source.lastValue.get());
    }

    @Override
    public Gauge64Recorder clone() {
        return new Gauge64Recorder(this);
    }

    protected void writeValue(final long value){
        updateReservoir(value);
        maxValue.accumulateAndGet(value, Math::max);
        minValue.accumulateAndGet(value, Math::min);
    }

    public final void updateValue(final LongUnaryOperator operator) {
        long current, next;
        do {
            next = operator.applyAsLong(current = lastValue.get());
        } while (!lastValue.compareAndSet(current, next));
        writeValue(next);
    }

    public final void updateValue(final LongBinaryOperator operator, final long value) {
        writeValue(lastValue.accumulateAndGet(value, operator));
    }

    @Override
    public final void accept(final long value){
        lastValue.set(value);
        writeValue(value);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        maxValue.set(Long.MIN_VALUE);
        minValue.set(Long.MAX_VALUE);
        lastValue.set(0L);
    }

    /**
     * Gets maximum value ever presented.
     *
     * @return Maximum value ever presented.
     */
    @Override
    public final long getMaxValue() {
        return maxValue.get();
    }

    /**
     * Gets maximum value for the last period.
     *
     * @param interval Period.
     * @return Maximum value of the last period.
     */
    @Override
    public final long getLastMaxValue(final MetricsInterval interval) {
        return 0;
    }

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    public final long getMinValue() {
        return minValue.get();
    }

    /**
     * Gets minimum value for the last period.
     *
     * @param interval Period.
     * @return Minimum value for the last period.
     */
    @Override
    public final long getLastMinValue(final MetricsInterval interval) {
        return 0;
    }

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    public final long getLastValue() {
        return lastValue.get();
    }
}
