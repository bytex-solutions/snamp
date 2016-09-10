package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of type {@link String}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class StringGaugeRecorder extends GaugeImpl<String> implements StringGauge {
    /**
     * Initializes a new string gauge.
     * @param name The name of the gauge.
     */
    public StringGaugeRecorder(final String name) {
        super(name, "");
    }
}
