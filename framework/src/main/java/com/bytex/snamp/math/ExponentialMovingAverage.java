package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;
import com.google.common.util.concurrent.AtomicDouble;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongSupplier;

/**
 * An exponentially-weighted moving average.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">Exponential moving average</a>
 */
public final class ExponentialMovingAverage extends AtomicDouble implements DoubleConsumer, DoubleSupplier, LongSupplier, Stateful, DoubleUnaryOperator, Cloneable {
    private static final Duration PRECISION = Duration.ofSeconds(1L);
    private static final Duration INTERVAL = PRECISION.multipliedBy(1L);
    private static final long serialVersionUID = -8885874345563930420L;

    private final AtomicDouble accumulator;
    private final double alpha;
    private final AtomicLong startTime;

    private ExponentialMovingAverage(final double intervalSeconds){
        super(0D);
        startTime = new AtomicLong(System.nanoTime());
        accumulator = new AtomicDouble(Double.NaN);
        alpha = 1 - Math.exp(-INTERVAL.getSeconds() / intervalSeconds);
    }

    /**
     * Initializes a new average calculator.
     * @param interval Interval of time, over which the reading is said to be averaged
     */
    public ExponentialMovingAverage(final Duration interval){
        this(interval.getSeconds());
    }

    public ExponentialMovingAverage(final long interval, final TimeUnit unit){
        this(unit.toSeconds(interval));
    }

    private ExponentialMovingAverage(final ExponentialMovingAverage source){
        super(source.get());
        accumulator = new AtomicDouble(source.accumulator.get());
        alpha = source.alpha;
        startTime = source.startTime;
    }

    public ExponentialMovingAverage clone(){
        return new ExponentialMovingAverage(this);
    }

    private double getAverage() {
        final double instantCount = getAndSet(0D) / INTERVAL.toNanos();
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
        addAndGet(value);
    }

    /**
     * Gets weighted average as {@code long} value.
     * @return The weighted average as {@code long} value.
     */
    @Override
    public long getAsLong() {
        return Math.round(getAsDouble());
    }

    /**
     * Gets weighted average.
     * @return The weighted average.
     */
    @Override
    public double getAsDouble() {
        final long currentTime = System.nanoTime();
        final long startTime = this.startTime.get();
        final long age = currentTime - startTime;
        double result = accumulator.get();
        if (age > INTERVAL.toNanos()) {
            final long newStartTime = currentTime - age % INTERVAL.toNanos();
            if (this.startTime.compareAndSet(startTime, newStartTime)) {
                for (int i = 0; i < age / INTERVAL.toNanos(); i++)
                    result = getAverage();
            }
        } else if (Double.isNaN(result))
            result = 0D;
        return result * PRECISION.toNanos();
    }


    @Override
    public double applyAsDouble(final double value) {
        accept(value);
        return getAsDouble();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        this.startTime.set(System.nanoTime());
        set(0D);
    }
}
