package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;
import com.bytex.snamp.concurrent.TimeLimitedObject;
import com.bytex.snamp.math.ExponentialMovingAverage;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

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

    final TimeLimitedLong createdAdder(final long initialValue){
        return TimeLimitedLong.adder(initialValue, timeToLive);
    }

    final ExponentialMovingAverage createEMA(){
        return new ExponentialMovingAverage(Duration.ofMillis(timeToLive));
    }

    final double divideFP(final Duration value) {
        return value.toMillis() / (double) timeToLive;
    }

    final TimeLimitedLong createPeakCounter(final long initialValue) {
        return TimeLimitedLong.peak(initialValue, timeToLive);
    }

    final <V>TimeLimitedObject<V> createTemporaryBox(final V initialValue, final BinaryOperator<V> operator){
        return new TimeLimitedObject<>(timeToLive, initialValue, operator);
    }
}
