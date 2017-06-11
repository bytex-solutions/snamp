package com.bytex.snamp.moa;

import com.bytex.snamp.concurrent.Timeout;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MeanRate extends Timeout implements DoubleSupplier, Cloneable {
    private static final long serialVersionUID = -9054381506116840089L;
    private final AtomicLong counter;
    private final Average average;

    public MeanRate(final Duration meanLifetime, final Duration measurementInterval) {
        super(measurementInterval);
        counter = new AtomicLong(0L);
        average = new TimeBasedDoubleEMA(meanLifetime, measurementInterval);
    }

    private MeanRate(final MeanRate other){
        super(other);
        counter = new AtomicLong(other.counter.get());
        average = other.average.clone();
    }

    private void computeAverage(final long newCounterValue){
        average.accept(counter.getAndSet(newCounterValue));
    }

    public void mark() {
        if (resetTimerIfExpired())
            computeAverage(1L);
        else
            counter.incrementAndGet();
    }

    /**
     * Gets average mean rate.
     * @return Average mean rate.
     */
    @Override
    public double getAsDouble() {
        if (resetTimerIfExpired())
            computeAverage(0L);
        return average.doubleValue();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        super.reset();
        counter.set(0L);
    }

    @Override
    public String toString() {
        return average.doubleValue() + "/" + getTimeout();
    }
}
