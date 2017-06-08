package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;

/**
 * Represents basic class for all implementations of exponntial moving average.
 * @author Roman Sakno
 * @since 2.0.
 * @version 2.0
 */
public abstract class AbstractEMA extends Number implements DoubleConsumer, Stateful, Cloneable {
    static final Duration DEFAULT_PRECISION = Duration.ofSeconds(1L);
    static final Duration DEFAULT_INTERVAL = DEFAULT_PRECISION.multipliedBy(1L);
    private final AtomicLong startTime;
    final long measurementIntervalNanos;

    AbstractEMA(final Duration measurementInterval){
        startTime = new AtomicLong(System.nanoTime());
        measurementIntervalNanos = measurementInterval.toNanos();
    }

    AbstractEMA(final AbstractEMA other) {
        startTime = new AtomicLong(other.startTime.get());
        measurementIntervalNanos = other.measurementIntervalNanos;
    }

    @Override
    public abstract AbstractEMA clone();

    final long getStartTime(){
        return startTime.get();
    }

    final boolean setStartTime(final long currentValue, final long newValue) {
        return startTime.compareAndSet(currentValue, newValue);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        startTime.set(System.nanoTime());
    }

    /**
     * Computes average value.
     * @return Average value.
     */
    @Override
    public abstract double doubleValue();

    @Override
    public int intValue() {
        return Math.round(floatValue());
    }

    @Override
    public long longValue() {
        return Math.round(doubleValue());
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }
}
