package com.bytex.snamp.concurrent;

/**
 * Represents abstract time-based accumulator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractAccumulator extends Number {
    private static final long serialVersionUID = 2991679442787059338L;
    private volatile long timer;

    /**
     * Time-to-live of the value in this accumulator, in millis.
     */
    private final long timeToLive;

    AbstractAccumulator(final long ttl){
        timer = System.currentTimeMillis();
        this.timeToLive = ttl;
    }

    void reset(){
        timer = System.currentTimeMillis();
    }

    /**
     * Determines whether the value in this accumulator is expired.
     * @return {@literal true}, if value is expired; otherwise, {@literal false}.
     */
    protected final boolean isExpired() {
        return System.currentTimeMillis() - timer > timeToLive;
    }
}
