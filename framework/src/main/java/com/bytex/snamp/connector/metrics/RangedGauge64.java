package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface RangedGauge64 extends Ranged, RatedGauge64 {
    @Override
    RangedGauge64 clone();
}
