package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedDouble;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents implementation of {@link GaugeFP}.
 * @since 2.0
 * @version 2.0
 */
public class GaugeFPRecorder extends AbstractNumericGauge implements GaugeFP, DoubleConsumer {
    private static final long serialVersionUID = 8109332123969613035L;
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

    protected GaugeFPRecorder(final GaugeFPRecorder source){
        super(source);
        maxValue = new AtomicDouble(source.maxValue.get());
        minValue = new AtomicDouble(source.minValue.get());
        lastValue = new AtomicDouble(source.lastValue.get());
        lastMaxValues = new MetricsIntervalMap<>(source.lastMaxValues, TimeLimitedDouble::clone);
        lastMinValues = new MetricsIntervalMap<>(source.lastMinValues, TimeLimitedDouble::clone);
    }

    @Override
    public GaugeFPRecorder clone() {
        return new GaugeFPRecorder(this);
    }

    protected void writeValue(final double value){
        updateReservoir(value);
        accumulate(maxValue, value, Math::max);
        accumulate(minValue, value, Math::min);
        lastMaxValues.forEachAcceptDouble(value, TimeLimitedDouble::accept);
        lastMinValues.forEachAcceptDouble(value, TimeLimitedDouble::accept);
    }

    public final double updateValue(final DoubleUnaryOperator operator){
        double current, next;
        do{
            next = operator.applyAsDouble(current = lastValue.get());
        } while (!lastValue.compareAndSet(current, next));
        writeValue(next);
        return next;
    }

    public final double updateValue(final DoubleBinaryOperator operator, final double value) {
        double current, next;
        do{
            next = operator.applyAsDouble(current = lastValue.get(), value);
        } while (!lastValue.compareAndSet(current, next));
        writeValue(next);
        return next;
    }

    @Override
    public final void accept(final double value) {
        lastValue.set(value);
        writeValue(value);
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
    public final double getMaxValue() {
        return maxValue.get();
    }

    @Override
    public final double getLastMaxValue(final MetricsInterval interval) {
        return lastMaxValues.getAsDouble(interval, TimeLimitedDouble::getAsDouble);
    }

    @Override
    public final double getMinValue() {
        return minValue.get();
    }

    @Override
    public final double getLastMinValue(final MetricsInterval interval) {
        return lastMinValues.getAsDouble(interval, TimeLimitedDouble::getAsDouble);
    }

    @Override
    public final double getLastValue() {
        return lastValue.get();
    }
}
