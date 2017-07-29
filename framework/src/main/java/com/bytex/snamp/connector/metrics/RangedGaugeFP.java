package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface RangedGaugeFP extends Ranged, RatedGaugeFP {
    @Override
    RangedGaugeFP clone();
}
