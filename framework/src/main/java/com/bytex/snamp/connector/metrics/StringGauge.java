package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of type {@link String}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class StringGauge extends GaugeImpl<String> {
    public StringGauge(final String name) {
        super(name, "");
    }
}
