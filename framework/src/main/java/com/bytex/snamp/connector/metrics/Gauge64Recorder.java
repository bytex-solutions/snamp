package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.math.ExponentialMovingAverage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents implementation of {@link Gauge64}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Gauge64Recorder extends AbstractNumericGauge implements Gauge64 {
    private final AtomicLong maxValue;
    private final AtomicLong minValue;
    private final MetricsIntervalMap<ExponentialMovingAverage> meanValues;
    private final AtomicLong lastValue;

    Gauge64Recorder(final String name, final int samplingSize) {
        super(name, samplingSize);
        maxValue = new AtomicLong(Long.MIN_VALUE);
        minValue = new AtomicLong(Long.MAX_VALUE);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        lastValue = new AtomicLong(0L);
    }

    public void update(final long value){
        updateReservoir(value);
        maxValue.accumulateAndGet(value, Math::max);
        minValue.accumulateAndGet(value, Math::min);
        lastValue.set(value);
        for(final MetricsInterval interval: MetricsInterval.ALL_INTERVALS){
            meanValues.get(interval).accept(value);
        }
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        super.reset();
        maxValue.set(Long.MIN_VALUE);
        minValue.set(Long.MAX_VALUE);
        meanValues.applyToAllIntervals(ExponentialMovingAverage::reset);
        lastValue.set(0L);
    }

    @Override
    public double getMeanValue(final MetricsInterval interval) {
        return meanValues.get(interval).getAsDouble();
    }

    /**
     * Gets maximum value ever presented.
     *
     * @return Maximum value ever presented.
     */
    @Override
    public long getMaxValue() {
        return maxValue.get();
    }

    /**
     * Gets maximum value for the last period of time.
     *
     * @param interval Period of time.
     * @return Maximum value of the last period of time.
     */
    @Override
    public long getLastMaxValue(final MetricsInterval interval) {
        return 0;
    }

    /**
     * The minimum value ever presented.
     *
     * @return The minimum value ever presented.
     */
    @Override
    public long getMinValue() {
        return minValue.get();
    }

    /**
     * Gets minimum value for the last period of time.
     *
     * @param interval Period of time.
     * @return Minimum value for the last period of time.
     */
    @Override
    public long getLastMinValue(final MetricsInterval interval) {
        return 0;
    }

    /**
     * The last presented value.
     *
     * @return The last presented value.
     */
    @Override
    public long getLastValue() {
        return lastValue.get();
    }
}
