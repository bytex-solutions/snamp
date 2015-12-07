package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

/**
 * Represents time interval for metrics.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum MetricsInterval {
    PER_SECOND(1000L),
    PER_MINUTE(60L * 1000L),
    PER_HOUR(60L * 60L * 1000L),
    PER_DAY(24L * 60L * 60L * 1000L);

    private final long timeToLive;

    MetricsInterval(final long ttlInMillis){
        this.timeToLive = ttlInMillis;
    }

    final LongAccumulator createAccumulator(){
        return LongAccumulator.adder(0L, timeToLive);
    }
}
