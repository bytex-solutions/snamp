package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;

/**
 * Represents basic class for all implementations of exponential moving average.
 * @author Roman Sakno
 * @since 2.0.
 * @version 2.0
 */
public abstract class EWMA extends Average implements Stateful, Cloneable, DoubleConsumer {
    private static final long serialVersionUID = -2018143871044363564L;

    private final AtomicLong startTime;
    final long measurementIntervalNanos;
    final double alpha;
    private final Duration meanLifetime;
    final long precisionNanos;

    EWMA(final Duration meanLifetime,
         final Duration measurementInterval,
         final TemporalUnit precision){
        precisionNanos = precision.getDuration().toNanos();
        startTime = new AtomicLong(System.nanoTime());
        measurementIntervalNanos = measurementInterval.toNanos();
        this.meanLifetime = Objects.requireNonNull(meanLifetime);
        alpha = computeAlpha(meanLifetime, measurementInterval);
    }

    EWMA(final EWMA other) {
        startTime = new AtomicLong(other.startTime.get());
        measurementIntervalNanos = other.measurementIntervalNanos;
        meanLifetime = other.getMeanLifetime();
        alpha = other.alpha;
        precisionNanos = other.precisionNanos;
    }

    private static double computeAlpha(final double meanLifetimeNanos, final double measurementIntervalNanos){
        return 1 - Math.exp(-measurementIntervalNanos / meanLifetimeNanos);
    }

    private static double computeAlpha(final Duration meanLifetime,
                                       final Duration measurementInterval) {
        return computeAlpha(meanLifetime.toNanos(), measurementInterval.toNanos());
    }

    public final Duration getMeanLifetime(){
        return meanLifetime;
    }

    @Override
    public abstract EWMA clone();

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
     * Adds value to existing accumulated value inside of measurement interval.
     * @param value Value to append.
     * @implSpec If two value will be supplied at the same measurement interval then they will be summarized.
     */
    public abstract void append(final double value);

    /**
     * Sets measured value within measurement interval.
     * @param value A value arrived at measurement interval.
     * @implSpec If two values will be supplied at the same measurement interval then only last value will be take into account.
     */
    public abstract void accept(final double value);
}
