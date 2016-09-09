package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;
import com.google.common.util.concurrent.AtomicDouble;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * An exponentially-weighted moving average.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">Exponential moving average</a>
 */
public final class ExponentialMovingAverage extends AtomicDouble implements DoubleConsumer, DoubleSupplier, LongSupplier, Stateful {
    private static final Duration PRECISION = Duration.ofSeconds(1L);
    private static final Duration INTERVAL = PRECISION.multipliedBy(1L);
    private static final long serialVersionUID = -8885874345563930420L;

    private final AtomicBoolean isSet;
    private volatile double accumulator;
    private final double alpha;
    private final AtomicLong startTime;

    /**
     * Initializes a new average calculator.
     * @param interval Period of time in minutes over which the reading is said to be averaged
     */
    public ExponentialMovingAverage(final Duration interval){
        super(0D);
        isSet = new AtomicBoolean(false);
        startTime = new AtomicLong(System.nanoTime());
        alpha = 1 - Math.exp(-INTERVAL.getSeconds() / (double)interval.getSeconds());
    }

    private double addAndGetAverage(final double state) {
        final double instantCount = getAndSet(0D) / INTERVAL.toNanos();
        return isSet.compareAndSet(false, true) ? instantCount : state + (alpha * (instantCount - state));
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
        double result = accumulator;
        if (age > INTERVAL.toNanos()) {
            final long newStartTime = currentTime - age % INTERVAL.toNanos();
            if (this.startTime.compareAndSet(startTime, newStartTime)) {
                for (int i = 0; i < age / INTERVAL.toNanos(); i++)
                    result = addAndGetAverage(result);
                accumulator = result;
            }
        }
        return result * PRECISION.toNanos();
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
