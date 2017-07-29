package com.bytex.snamp.instrumentation.measurements.jmx;

import com.bytex.snamp.instrumentation.measurements.TimeMeasurement;

import java.util.Objects;

/**
 * Represents measurement of time.
 * @since 2.0
 * @version 2.1
 */
public class TimeMeasurementNotification extends MeasurementNotification<TimeMeasurement> {
    public static final String TYPE = "com.bytex.snamp.measurement.stopwatch";
    private static final long serialVersionUID = -5796311143102532739L;
    private final TimeMeasurement measurement;

    public TimeMeasurementNotification(final Object source, final TimeMeasurement measurement) {
        this(TYPE, source, measurement, "Time measurement supplied");
    }

    public TimeMeasurementNotification(final Object source){
        this(source, new TimeMeasurement());
    }

    TimeMeasurementNotification(final String type, final Object source, final TimeMeasurement measurement, final String message) {
        super(type, source, message);
        this.measurement = Objects.requireNonNull(measurement);
    }

    @Override
    public TimeMeasurement getMeasurement() {
        return measurement;
    }
}
