package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of type {@link String}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class StringGauge extends GaugeImpl<String> {
    /**
     * Initializes a new string gauge.
     * @param name The name of the gauge.
     */
    public StringGauge(final String name) {
        super(name, "");
    }
}
