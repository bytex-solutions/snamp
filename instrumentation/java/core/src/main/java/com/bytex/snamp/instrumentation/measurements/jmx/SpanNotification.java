package com.bytex.snamp.instrumentation.measurements.jmx;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;

import java.util.Objects;

/**
 * Represents event with description of span occurred in program.
 * @since 2.0
 * @version 2.1
 */
public final class SpanNotification extends TimeMeasurementNotification {
    public static final String TYPE = "com.bytex.snamp.measurement.span";
    private static final long serialVersionUID = 6318832561777725751L;
    private final Span measurement;

    public SpanNotification(final Object source, final Span measurement) {
        super(TYPE, source, measurement, "Span detected");
        this.measurement = Objects.requireNonNull(measurement);
    }

    public SpanNotification(final Object source){
        this(source, new Span());
    }

    @Override
    public Span getMeasurement() {
        return measurement;
    }

    public Identifier getSpanID(){
        return getMeasurement().getSpanID();
    }

    public Identifier getParentSpanID(){
        return getMeasurement().getParentSpanID();
    }

    public Identifier getCorrelationID(){
        return getMeasurement().getCorrelationID();
    }
}
