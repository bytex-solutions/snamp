package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.moa.DoubleEWMA;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

/**
 * Computes mean rate using exponential moving average.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MeanRate extends AtomicLong implements DoubleSupplier, Cloneable, Serializable, Stateful {
    private static final long serialVersionUID = -9054381506116840089L;
    private final DoubleEWMA average;
    private final Duration measurementInterval;
    private final AtomicLong timer;

    MeanRate(final Duration meanLifetime, final Duration measurementInterval) {
        super(0L);
        average = DoubleEWMA.fixedInterval(meanLifetime, measurementInterval);
        this.measurementInterval = measurementInterval;
        timer = new AtomicLong(System.nanoTime());
    }

    private MeanRate(final MeanRate other){
        super(other.get());
        average = other.average.clone();
        measurementInterval = other.measurementInterval;
        timer = new AtomicLong(other.timer.get());
    }

    @Override
    protected MeanRate clone(){
        return new MeanRate(this);
    }

    void mark() {
        incrementAndGet();
    }

    /**
     * Gets average mean rate.
     * @return Average mean rate.
     */
    @Override
    public double getAsDouble() {
        final long currentTime = System.nanoTime();
        final long lastAccessTime = timer.getAndSet(currentTime);
        final long series = (currentTime - lastAccessTime) / measurementInterval.toNanos();
        double result = 0D;
        final double perInterval = (double) getAndSet(0L) / series;
        for (int i = 0; i < series; i++) {
            result = average.applyAsDouble(perInterval);
        }
        return result;
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        set(0L);
        timer.set(System.nanoTime());
    }

    @Override
    public String toString() {
        return average.doubleValue() + "/" + measurementInterval;
    }
}
