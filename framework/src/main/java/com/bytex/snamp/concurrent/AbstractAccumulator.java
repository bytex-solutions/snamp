package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

/**
 * Represents abstract time-based accumulator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAccumulator extends Number {
    private static final long serialVersionUID = 2991679442787059338L;
    @SpecialUse
    private volatile long timer;

    /**
     * Time-to-live of the value in this accumulator, in millis.
     */
    private final long timeToLive;

    AbstractAccumulator(final long ttl){
        timer = System.currentTimeMillis();
        this.timeToLive = ttl;
    }

    final boolean isExpired(final boolean updateTimer) {
        final long startTime = timer;
        final long currentTime = System.currentTimeMillis();
        if (currentTime - startTime > timeToLive) {
            if (updateTimer) timer = startTime;
            return true;
        } else return false;
    }
}
