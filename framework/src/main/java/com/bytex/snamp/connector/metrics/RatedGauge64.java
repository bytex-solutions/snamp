package com.bytex.snamp.connector.metrics;

/**
 * Represents {@link Gauge64} that rates input stream of {@code long} values.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface RatedGauge64 extends Gauge64, Rate {
    @Override
    RatedGauge64 clone();
}
