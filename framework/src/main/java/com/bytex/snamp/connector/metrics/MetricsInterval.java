package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;
import com.bytex.snamp.math.ExponentiallyMovingAverage;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Represents time interval for metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum MetricsInterval {
    SECOND(1, TimeUnit.SECONDS),
    MINUTE(1, TimeUnit.MINUTES),
    FIVE_MINUTES(5, TimeUnit.MINUTES),
    FIFTEEN_MINUTES(15, TimeUnit.MINUTES),
    HOUR(1, TimeUnit.HOURS),
    TWELVE_HOURS(12, TimeUnit.HOURS),
    DAY(1, TimeUnit.DAYS);

    private final long timeToLive;

    MetricsInterval(final long amount, final TimeUnit unit){
        this.timeToLive = unit.toMillis(amount);
    }

    final LongAccumulator createdAdder(final long initialValue){
        return LongAccumulator.adder(initialValue, timeToLive);
    }

    final ExponentiallyMovingAverage createEMA(){
        return new ExponentiallyMovingAverage(Duration.ofMillis(timeToLive));
    }

    final double divideFP(final Duration value) {
        return value.toMillis() / (double) timeToLive;
    }

    final LongAccumulator createPeakCounter(final long initialValue) {
        return LongAccumulator.peak(initialValue, timeToLive);
    }
}
