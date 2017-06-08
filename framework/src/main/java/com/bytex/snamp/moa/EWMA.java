package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;

/**
 * Represents basic class for all implementations of exponntial moving average.
 * @author Roman Sakno
 * @since 2.0.
 * @version 2.0
 */
public abstract class EWMA extends Number implements DoubleConsumer, Stateful, Cloneable {
    static final Duration DEFAULT_PRECISION = Duration.ofSeconds(1L);
    static final Duration DEFAULT_INTERVAL = DEFAULT_PRECISION.multipliedBy(1L);
    private static final long serialVersionUID = -2018143871044363564L;

    private final AtomicLong startTime;
    final long measurementIntervalNanos;
    final double alpha;
    private final Duration meanLifetime;

    EWMA(final Duration meanLifetime, final Duration measurementInterval){
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
    }

    private static double computeAlpha(final double meanLifetimeSec, final double measurementIntervalSec){
        return 1 - Math.exp(-measurementIntervalSec / meanLifetimeSec);
    }

    private static double computeAlpha(final Duration meanLifetime, final Duration measurementInterval) {
        return computeAlpha(meanLifetime.get(ChronoUnit.SECONDS), measurementInterval.get(ChronoUnit.SECONDS));
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
