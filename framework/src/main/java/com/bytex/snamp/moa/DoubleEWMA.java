package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;
import com.google.common.util.concurrent.AtomicDouble;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleUnaryOperator;

/**
 * An exponentially-weighted moving average based on {@code double} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">Exponential moving average</a>
 */
public final class DoubleEWMA extends EWMA implements DoubleConsumer, Stateful, DoubleUnaryOperator, Cloneable {
    private static final long serialVersionUID = -8885874345563930420L;

    private final AtomicDouble adder;
    private final AtomicDouble accumulator;
    private final long precisionNanos;

    private DoubleEWMA(final Duration meanLifetime,
                       final Duration precision,
                       final Duration measurementInterval) {
        super(meanLifetime, measurementInterval);
        adder = new AtomicDouble(0D);
        accumulator = new AtomicDouble(Double.NaN);
        precisionNanos = precision.toNanos();
    }

    /**
     * Initializes a new average calculator.
     * @param meanLifetime Interval of time, over which the reading is said to be averaged. Cannot be {@literal null}.
     */
    public DoubleEWMA(final Duration meanLifetime){
        this(meanLifetime, DEFAULT_PRECISION, DEFAULT_INTERVAL);
    }

    public DoubleEWMA(final long interval, final TemporalUnit unit) {
        this(Duration.of(interval, unit), DEFAULT_PRECISION, DEFAULT_INTERVAL);
    }

    private DoubleEWMA(final DoubleEWMA source){
        super(source);
        adder = new AtomicDouble(source.adder.get());
        accumulator = new AtomicDouble(source.accumulator.get());
        precisionNanos = source.precisionNanos;
    }

    public DoubleEWMA clone(){
        return new DoubleEWMA(this);
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
