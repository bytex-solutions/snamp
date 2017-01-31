package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedDouble;
import com.bytex.snamp.concurrent.TimeLimitedLong;
import com.bytex.snamp.concurrent.TimeLimitedObject;
import com.bytex.snamp.moa.ExponentialMovingAverage;
import com.google.common.collect.ImmutableSortedSet;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

/**
 * Represents time interval for metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum MetricsInterval implements Comparable<MetricsInterval>, Serializable {
    SECOND(1, TimeUnit.SECONDS){
        @Override
        public String toString() {
            return "Second";
        }
    },
    MINUTE(1, TimeUnit.MINUTES){
        @Override
        public String toString() {
            return "Minute";
        }
    },
    FIVE_MINUTES(5, TimeUnit.MINUTES){
        @Override
        public String toString() {
            return "5Minutes";
        }
    },
    FIFTEEN_MINUTES(15, TimeUnit.MINUTES){
        @Override
        public String toString() {
            return "15Minutes";
        }
    },
    HOUR(1, TimeUnit.HOURS){
        @Override
        public String toString() {
            return "Hour";
        }
    },
    TWELVE_HOURS(12, TimeUnit.HOURS){
        @Override
        public String toString() {
            return "12Hours";
        }
    },
    DAY(1, TimeUnit.DAYS){
        @Override
        public String toString() {
            return "Day";
        }
    };

    /**
     * Sorted set of available intervals for fast iteration.
     */
    public static final ImmutableSortedSet<MetricsInterval> ALL_INTERVALS = ImmutableSortedSet.copyOf(MetricsInterval::compareTo, Arrays.asList(values()));

    private final long timeToLive;

    MetricsInterval(final long amount, final TimeUnit unit){
        this.timeToLive = unit.toMillis(amount);
    }

    final TimeLimitedLong createdAdder(final long initialValue){
        return TimeLimitedLong.adder(initialValue, Duration.ofMillis(timeToLive));
    }

    final TimeLimitedLong createLongPeakDetector(final long initialValue){
        return TimeLimitedLong.peak(initialValue, Duration.ofMillis(timeToLive));
    }

    final ExponentialMovingAverage createEMA(){
        return new ExponentialMovingAverage(Duration.ofMillis(timeToLive));
    }

    final double divideFP(final Duration value) {
        return divideFP(value.toMillis());
    }

    final double divideFP(final double value){
        return value / timeToLive;
    }

    final TimeLimitedDouble createDoublePeakDetector(final double initialValue){
        return TimeLimitedDouble.peak(initialValue, Duration.ofMillis(timeToLive));
    }

    final TimeLimitedDouble createDoubleMinDetector(final double initialValue){
        return TimeLimitedDouble.min(initialValue, Duration.ofMillis(timeToLive));
    }

    final <V>TimeLimitedObject<V> createTemporaryBox(final V initialValue, final BinaryOperator<V> operator){
        return new TimeLimitedObject<>(initialValue, Duration.ofMillis(timeToLive), operator);
    }

    /**
     * Gets next interval.
     * @return The next available interval.
     */
    public final MetricsInterval next(){
        for(final MetricsInterval interval: ALL_INTERVALS)
            if(interval.compareTo(this) > 0)
                return interval;
        return null;
    }

    /**
     * Gets a sorted set of intervals that are greater than this interval.
     * @return A sorted set of intervals.
     */
    public final ImmutableSortedSet<MetricsInterval> greater(){
        return ALL_INTERVALS.tailSet(this, false);
    }

    /**
     * Gets a sorted set of intervals that are less than this interval.
     * @return A sorted set of intervals.
     */
    public final ImmutableSortedSet<MetricsInterval> less(){
        return ALL_INTERVALS.headSet(this, false);
    }

    public abstract String toString();
}
