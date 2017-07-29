package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface RangedTimer extends RatedTimer, Ranged {
    @Override
    RangedTimer clone();
}
