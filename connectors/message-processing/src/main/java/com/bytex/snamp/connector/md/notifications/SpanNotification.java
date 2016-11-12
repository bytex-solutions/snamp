package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.Span;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents event with description of span occurred in program.
 * @since 2.0
 * @version 2.0
 */
public final class SpanNotification extends TimeMeasurementNotification {
    public static final String TYPE = "com.bytex.snamp.measurement.span";
    private static final long serialVersionUID = 6318832561777725751L;
    private final Span measurement;

    public SpanNotification(final Object source, final Span measurement) {
        super(TYPE, source, measurement);
        this.measurement = Objects.requireNonNull(measurement);
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

    public Duration getDuration() {
        return Duration.ofNanos(getMeasurement().getDuration(TimeUnit.NANOSECONDS));
    }
}
