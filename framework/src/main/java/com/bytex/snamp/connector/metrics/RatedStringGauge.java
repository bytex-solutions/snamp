package com.bytex.snamp.connector.metrics;

/**
 * Represents {@link StringGauge} that rates input stream of {@link String} values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface RatedStringGauge extends StringGauge, Rate {
    @Override
    RatedStringGauge clone();
}
