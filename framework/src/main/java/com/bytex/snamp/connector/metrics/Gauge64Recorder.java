package com.bytex.snamp.connector.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * Represents implementation of {@link Gauge64}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class Gauge64Recorder extends AbstractNumericGauge implements Gauge64, LongConsumer {
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

    @Override
    public void accept(final long value){
        updateReservoir(value);
        maxValue.accumulateAndGet(value, Math::max);
        minValue.accumulateAndGet(value, Math::min);
        lastValue.set(value);
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
     * Gets maximum value for the last period of time.
     *
     * @param interval Period of time.
     * @return Maximum value of the last period of time.
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
     * Gets minimum value for the last period of time.
     *
     * @param interval Period of time.
     * @return Minimum value for the last period of time.
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
