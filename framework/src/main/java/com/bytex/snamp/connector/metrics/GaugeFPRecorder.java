package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedDouble;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.function.DoubleBinaryOperator;

/**
 * Represents implementation of {@link GaugeFP}.
 * @since 2.0
 * @version 2.0
 */
public final class GaugeFPRecorder extends AbstractNumericGauge implements GaugeFP {
    private final AtomicDouble maxValue;
    private final AtomicDouble minValue;
    private final AtomicDouble lastValue;
    private final MetricsIntervalMap<TimeLimitedDouble> lastMaxValues;
    private final MetricsIntervalMap<TimeLimitedDouble> lastMinValues;

    public GaugeFPRecorder(final String name, final int samplingSize){
        super(name, samplingSize);
        maxValue = new AtomicDouble(Double.MIN_VALUE);
        minValue = new AtomicDouble(Double.MAX_VALUE);
        lastValue = new AtomicDouble(0L);
        lastMaxValues = new MetricsIntervalMap<>(interval -> interval.createDoublePeakDetector(Double.MIN_VALUE));
        lastMinValues = new MetricsIntervalMap<>(interval -> interval.createDoubleMinDetector(Double.MAX_VALUE));
    }

    public GaugeFPRecorder(final String name){
        this(name, DEFAULT_SAMPLING_SIZE);
    }

    public void update(final double value) {
        updateReservoir(value);
        accumulate(maxValue, value, Math::max);
        accumulate(minValue, value, Math::min);
        lastValue.set(value);
        lastMaxValues.forEachAcceptDouble(value, TimeLimitedDouble::accept);
        lastMinValues.forEachAcceptDouble(value, TimeLimitedDouble::accept);
    }

    private static void accumulate(final AtomicDouble thiz, final double value, final DoubleBinaryOperator operator) {
        double next, prev;
        do {
            next = operator.applyAsDouble(prev = thiz.get(), value);
        } while (!thiz.compareAndSet(prev, next));
    }

    @Override
    public void reset() {
        maxValue.set(Double.MIN_VALUE);
        minValue.set(Double.MAX_VALUE);
        lastValue.set(0L);
        lastMaxValues.values().forEach(TimeLimitedDouble::reset);
    }

    @Override
    public double getMaxValue() {
        return maxValue.get();
    }

    @Override
    public double getLastMaxValue(final MetricsInterval interval) {
        return lastMaxValues.getAsDouble(interval, TimeLimitedDouble::getAsDouble);
    }

    @Override
    public double getMinValue() {
        return minValue.get();
    }

    @Override
    public double getLastMinValue(final MetricsInterval interval) {
        return lastMinValues.getAsDouble(interval, TimeLimitedDouble::getAsDouble);
    }

    @Override
    public double getLastValue() {
        return lastValue.get();
    }
}
