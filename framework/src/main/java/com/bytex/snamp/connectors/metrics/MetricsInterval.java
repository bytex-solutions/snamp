package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

import java.util.concurrent.TimeUnit;

/**
 * Represents time interval for metrics.
 * @author Roman Sakno
 * @version 1.2
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

    final LongAccumulator createAccumulator(){
        return LongAccumulator.adder(0L, timeToLive);
    }
}
