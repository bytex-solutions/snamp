package com.itworks.snamp.concurrent;

/**
 * Represents abstract time-based accumulator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAccumulator extends Number {
    private static final long serialVersionUID = 2991679442787059338L;
    volatile long timer;
    /**
     * Time-to-live of the value in this accumulator, in millis.
     */
    protected final long timeToLive;

    protected AbstractAccumulator(final long ttl){
        timer = System.currentTimeMillis();
        this.timeToLive = ttl;
    }
}