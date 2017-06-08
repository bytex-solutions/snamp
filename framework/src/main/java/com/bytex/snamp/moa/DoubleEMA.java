package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;
import com.google.common.util.concurrent.AtomicDouble;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;

/**
 * An exponentially-weighted moving average based on {@code double} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">Exponential moving average</a>
 */
public final class DoubleEMA extends AbstractEMA implements DoubleConsumer, Stateful, DoubleUnaryOperator, Cloneable {
    private static final long serialVersionUID = -8885874345563930420L;

    private final AtomicDouble adder;
    private final AtomicDouble accumulator;
    private final double alpha;
    private final long precisionNanos;

    private DoubleEMA(final double intervalSeconds,
                      final Duration precision,
                      final Duration measurementInterval) {
        super(measurementInterval);
        adder = new AtomicDouble(0D);
        accumulator = new AtomicDouble(Double.NaN);
        alpha = 1 - Math.exp(-measurementInterval.getSeconds() / intervalSeconds);
        precisionNanos = precision.toNanos();
    }

    /**
     * Initializes a new average calculator.
     * @param interval Interval of time, over which the reading is said to be averaged
     */
    public DoubleEMA(final Duration interval){
        this(interval.getSeconds(), DEFAULT_PRECISION, DEFAULT_INTERVAL);
    }

    public DoubleEMA(final long interval, final TimeUnit unit){
        this(unit.toSeconds(interval), DEFAULT_PRECISION, DEFAULT_INTERVAL);
    }

    private DoubleEMA(final DoubleEMA source){
        super(source);
        adder = new AtomicDouble(source.adder.get());
        accumulator = new AtomicDouble(source.accumulator.get());
        alpha = source.alpha;
        precisionNanos = source.precisionNanos;
    }

    public DoubleEMA clone(){
        return new DoubleEMA(this);
    }

    private double getAverage() {
        final double instantCount = adder.getAndSet(0D) / measurementIntervalNanos;
        if(accumulator.compareAndSet(Double.NaN, instantCount)) //first time set
            return instantCount;
        else {
            double next, prev;
            do {
                prev = accumulator.get();
                next = prev + (alpha * (instantCount - prev));
            } while (!accumulator.compareAndSet(prev, next));
            return next;
        }
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final double value) {
        adder.addAndGet(value);
    }

    /**
     * Gets weighted average.
     * @return The weighted average.
     */
    @Override
    public double doubleValue() {
        final long currentTime = System.nanoTime();
        final long startTime = getStartTime();
        final long age = currentTime - startTime;
        double result = accumulator.get();
        if (age > measurementIntervalNanos) {
            final long newStartTime = currentTime - age % measurementIntervalNanos;
            if (setStartTime(startTime, newStartTime)) {
                for (int i = 0; i < age / measurementIntervalNanos; i++)
                    result = getAverage();
            }
        } else if (Double.isNaN(result))
            result = 0D;
        return result * precisionNanos;
    }


    @Override
    public double applyAsDouble(final double value) {
        accept(value);
        return doubleValue();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        super.reset();
        adder.set(0D);
        accumulator.set(Double.NaN);
    }
}
