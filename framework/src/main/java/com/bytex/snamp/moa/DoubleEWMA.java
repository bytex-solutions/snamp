package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.concurrent.Timeout;
import com.google.common.util.concurrent.AtomicDouble;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * An exponentially-weighted moving average based on {@code double} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see <a href="https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">Exponential moving average</a>
 */
public final class DoubleEWMA extends EWMA implements DoubleConsumer, Stateful, Cloneable {
    private static final long serialVersionUID = -8885874345563930420L;

    private static final class IntervalMemory extends Timeout implements DoubleSupplier {
        private static final long serialVersionUID = -3405864345563930421L;
        private final AtomicDouble memory;

        private IntervalMemory(final Duration ttl) {
            super(ttl);
            memory = new AtomicDouble(0D);
        }

        private IntervalMemory(final IntervalMemory other) {
            super(other);
            memory = new AtomicDouble(other.memory.get());
        }

        double swap(final double value) {
            if(resetTimerIfExpired())
                return memory.getAndSet(value);
            else {
                memory.set(value);
                return 0D;
            }
        }

        @Override
        public void reset() {
            super.reset();
            memory.set(0D);
        }

        @Override
        public double getAsDouble() {
            super.reset();
            return memory.getAndSet(0D);
        }
    }

    private final AtomicDouble adder;
    private final AtomicDouble accumulator;
    private final IntervalMemory memory;

    public DoubleEWMA(final Duration meanLifetime,
                       final Duration measurementInterval,
                       final TemporalUnit precision) {
        super(meanLifetime, measurementInterval, precision);
        adder = new AtomicDouble(0D);
        accumulator = new AtomicDouble(Double.NaN);
        memory = new IntervalMemory(measurementInterval);
    }

    /**
     * Initializes a new average calculator with measurement interval of 1 second.
     * @param meanLifetime Interval of time, over which the reading is said to be averaged. Cannot be {@literal null}.
     */
    public DoubleEWMA(final Duration meanLifetime){
        this(meanLifetime, Duration.ofSeconds(1), ChronoUnit.SECONDS);
    }

    private DoubleEWMA(final DoubleEWMA source) {
        super(source);
        adder = new AtomicDouble(source.adder.get());
        accumulator = new AtomicDouble(source.accumulator.get());
        memory = new IntervalMemory(source.memory);
    }

    public DoubleEWMA clone(){
        return new DoubleEWMA(this);
    }

    private double getAverage() {
        final double instantCount = (memory.getAsDouble() + adder.getAndSet(0D)) / measurementIntervalNanos;
        if (accumulator.compareAndSet(Double.NaN, instantCount)) //first time set
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

    @Override
    public void append(double value) {
        adder.addAndGet(value);
    }

    @Override
    public void accept(final double value) {
        append(memory.swap(value));
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

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        super.reset();
        adder.set(0D);
        accumulator.set(Double.NaN);
        memory.reset();
    }
}
