package com.bytex.snamp.connector.metrics;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Gauge64WithNormative extends Normative, RatedGauge64 {
    @Override
    Gauge64WithNormative clone();
}
