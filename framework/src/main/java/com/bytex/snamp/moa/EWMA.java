package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.ToLongFunction;

/**
 * Represents basic class for all implementations of exponential moving average.
 * @author Roman Sakno
 * @since 2.0.
 * @version 2.0
 */
public abstract class EWMA extends Number implements Stateful, Cloneable, DoubleConsumer {
    private static final long serialVersionUID = -2018143871044363564L;

    /**
     * Represents precision of EWMA calculations.
     */
    public enum Precision implements ToLongFunction<Duration> {
        /**
         * Precision is 1 second.
         */
        SECOND(ChronoUnit.SECONDS) {
            @Override
            public long applyAsLong(final Duration value) {
                return value.get(ChronoUnit.SECONDS);
            }
        },
        /**
         * Precision is 1 millisecond
         */
        MILLIS(ChronoUnit.MILLIS) {
            @Override
            public long applyAsLong(final Duration value) {
                return value.toMillis();
            }
        };

        private final ChronoUnit unit;

        Precision(final ChronoUnit unit){
            this.unit = unit;
        }

        final long toNanos(){
            return unit.getDuration().toNanos();
        }
    }

    private final AtomicLong startTime;
    final long measurementIntervalNanos;
    final double alpha;
    private final Duration meanLifetime;
    final long precisionNanos;

    EWMA(final Duration meanLifetime,
         final Duration measurementInterval,
         final Precision precision){
        precisionNanos = precision.toNanos();
        startTime = new AtomicLong(System.nanoTime());
        measurementIntervalNanos = measurementInterval.toNanos();
        this.meanLifetime = Objects.requireNonNull(meanLifetime);
        alpha = computeAlpha(meanLifetime, measurementInterval, precision);
    }

    EWMA(final EWMA other) {
        startTime = new AtomicLong(other.startTime.get());
        measurementIntervalNanos = other.measurementIntervalNanos;
        meanLifetime = other.getMeanLifetime();
        alpha = other.alpha;
        precisionNanos = other.precisionNanos;
    }

    private static double computeAlpha(final double meanLifetimeSec, final double measurementIntervalSec){
        return 1 - Math.exp(-measurementIntervalSec / meanLifetimeSec);
    }

    private static double computeAlpha(final Duration meanLifetime,
                                       final Duration measurementInterval,
                                       final Precision precision) {
        return computeAlpha(precision.applyAsLong(meanLifetime), precision.applyAsLong(measurementInterval));
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
