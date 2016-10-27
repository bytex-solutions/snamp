package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of type {@link String}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class StringGaugeRecorder extends GaugeImpl<String> implements StringGauge {
    private static final long serialVersionUID = -8088704775399041609L;

    /**
     * Initializes a new string gauge.
     * @param name The name of the gauge.
     */
    public StringGaugeRecorder(final String name) {
        super(name, "");
    }

    protected StringGaugeRecorder(final StringGaugeRecorder source){
        super(source);
    }

    @Override
    public StringGaugeRecorder clone() {
        return new StringGaugeRecorder(this);
    }
}
