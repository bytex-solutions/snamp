package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface TimerWithNormative extends RatedTimer, Normative {
    @Override
    TimerWithNormative clone();
}
