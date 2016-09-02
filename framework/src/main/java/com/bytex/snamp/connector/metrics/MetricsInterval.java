package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

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
    HOUR(1, TimeUnit.HOURS),
    DAY(1, TimeUnit.DAYS);

    private final long timeToLive;

    MetricsInterval(final long amount, final TimeUnit unit){
        this.timeToLive = unit.toMillis(amount);
    }

    double fromMillis(final double millis){
        return millis / timeToLive;
    }

    LongAccumulator createdAdder(){
        return LongAccumulator.adder(0L, timeToLive);
    }

}
