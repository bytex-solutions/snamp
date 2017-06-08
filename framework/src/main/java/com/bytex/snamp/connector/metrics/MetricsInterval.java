package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedDouble;
import com.bytex.snamp.concurrent.TimeLimitedLong;
import com.bytex.snamp.concurrent.TimeLimitedObject;
import com.bytex.snamp.moa.AbstractEMA;
import com.bytex.snamp.moa.DoubleEMA;
import com.google.common.collect.ImmutableSortedSet;

import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.function.BinaryOperator;

/**
 * Represents time interval for metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum MetricsInterval implements Comparable<MetricsInterval>, Serializable {
    SECOND(1, ChronoUnit.SECONDS){
        @Override
        public String toString() {
            return "Second";
        }
    },
    MINUTE(1, ChronoUnit.MINUTES){
        @Override
        public String toString() {
            return "Minute";
        }
    },
    FIVE_MINUTES(5, ChronoUnit.MINUTES){
        @Override
        public String toString() {
            return "5Minutes";
        }
    },
    QUARTER_OF_HOUR(15, ChronoUnit.MINUTES){
        @Override
        public String toString() {
            return "15Minutes";
        }
    },
    HOUR(1, ChronoUnit.HOURS){
        @Override
        public String toString() {
            return "Hour";
        }
    },
    HALF_DAY(1, ChronoUnit.HALF_DAYS){
        @Override
        public String toString() {
            return "12Hours";
        }
    },
    DAY(1, ChronoUnit.DAYS){
        @Override
        public String toString() {
            return "Day";
        }
    };

    /**
     * Sorted set of available intervals for fast iteration.
     */
    public static final ImmutableSortedSet<MetricsInterval> ALL_INTERVALS = ImmutableSortedSet.copyOf(MetricsInterval::compareTo, Arrays.asList(values()));

    /**
     * Represents duration of interval.
     */
    public final Duration duration;

    MetricsInterval(final long amount, final TemporalUnit unit){
        this.duration = Duration.of(amount, unit);
    }

    final TimeLimitedLong createdAdder(final long initialValue){
        return TimeLimitedLong.adder(initialValue, duration);
    }

    final TimeLimitedLong createLongPeakDetector(final long initialValue){
        return TimeLimitedLong.peak(initialValue, duration);
    }

    final AbstractEMA createEMA(){
        return new DoubleEMA(duration);
    }

    final double divideFP(final Duration value) {
        return divideFP(value.toMillis());
    }

    final double divideFP(final double value){
        return value / duration.toMillis();
    }

    final TimeLimitedDouble createDoublePeakDetector(final double initialValue){
        return TimeLimitedDouble.peak(initialValue, duration);
    }

    final TimeLimitedDouble createDoubleMinDetector(final double initialValue){
        return TimeLimitedDouble.min(initialValue, duration);
    }

    final <V>TimeLimitedObject<V> createTemporaryBox(final V initialValue, final BinaryOperator<V> operator){
        return new TimeLimitedObject<>(initialValue, duration, operator);
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
