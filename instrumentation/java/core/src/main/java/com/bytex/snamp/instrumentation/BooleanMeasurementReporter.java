package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.BooleanMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents reporter for measurement of type {@code boolean}.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class BooleanMeasurementReporter extends ValueMeasurementReporter<BooleanMeasurement> {
    protected BooleanMeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param userData Additional data associated with measurement.
     */
    public void report(final boolean value, final Map<String, String> userData){
        final BooleanMeasurement measurement = new BooleanMeasurement(value);
        measurement.setAnnotations(userData);
        report(measurement);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     */
    public final void report(final boolean value){
        report(value, Collections.<String, String>emptyMap());
    }

    public RuntimeScope scheduleReporting(final ReportingTask<? super BooleanMeasurementReporter> task, final long delay, final TimeUnit unit){
        return scheduleReporting(createTask(this, task), delay, unit);
    }
}
