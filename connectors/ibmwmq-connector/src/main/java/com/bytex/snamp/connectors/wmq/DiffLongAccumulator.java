package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.TimeSpan;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class DiffLongAccumulator {
    long timer;
    /**
     * Time-to-live of the value in this accumulator, in millis.
     */
    protected final long timeToLive;

    private long current;

    /**
     * Initializes a new accumulator of {@literal long} values.
     *
     * @param ttl          Time-to-live of the value in this accumulator. Cannot be {@literal null}.
     */
    DiffLongAccumulator(final TimeSpan ttl){
        timeToLive = ttl.toMillis();
        timer = System.currentTimeMillis();
        current = 0L;
    }

    /**
     * Modifies this accumulator.
     * @param value A new value to be combined with existing accumulator value.
     * @return Modified accumulator value.
     */
    synchronized long update(final long value) {
        final long currentTime = System.currentTimeMillis();
        if (current == 0L || (currentTime - timer > timeToLive)) {
            current = value;
            timer = System.currentTimeMillis();
            return 0L;
        } else return value - current;
    }
}
