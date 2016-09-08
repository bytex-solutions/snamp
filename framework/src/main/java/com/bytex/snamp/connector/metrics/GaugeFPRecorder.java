package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedDouble;
import com.bytex.snamp.math.ExponentialMovingAverage;
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
    private final MetricsIntervalMap<ExponentialMovingAverage> meanValues;
    private final MetricsIntervalMap<TimeLimitedDouble> lastMaxValues;
    private final MetricsIntervalMap<TimeLimitedDouble> lastMinValues;

    public GaugeFPRecorder(final String name){
        super(name);
        maxValue = new AtomicDouble(Double.MIN_VALUE);
        minValue = new AtomicDouble(Double.MAX_VALUE);
        lastValue = new AtomicDouble(0L);
        meanValues = new MetricsIntervalMap<>(MetricsInterval::createEMA);
        lastMaxValues = new MetricsIntervalMap<>(interval -> interval.createDoublePeakDetector(Double.MIN_VALUE));
        lastMinValues = new MetricsIntervalMap<>(interval -> interval.createDoubleMinDetector(Double.MAX_VALUE));
    }

    public void update(final double value) {
        accumulate(maxValue, value, Math::max);
        accumulate(minValue, value, Math::min);
        lastValue.set(value);
        meanValues.forEachAccept(value, ExponentialMovingAverage::accept);
        lastMaxValues.forEachAccept(value, TimeLimitedDouble::accept);
        lastMinValues.forEachAccept(value, TimeLimitedDouble::accept);
    }

    @Override
    public double getQuantile(double quantile) {
        return 0;
    }

    @Override
    public double getDeviation() {
        return 0;
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
        meanValues.values().forEach(ExponentialMovingAverage::reset);
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

    @Override
    public double getMeanValue(final MetricsInterval interval) {
        return meanValues.getAsDouble(interval, ExponentialMovingAverage::getAsDouble);
    }
}
