package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.ChangeType;
import com.bytex.snamp.instrumentation.measurements.FloatingPointMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents reporter for measurement of type {@code double}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FloatingPointMeasurementReporter extends ValueMeasurementReporter<FloatingPointMeasurement> {
    protected FloatingPointMeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     */
    public final void report(final double value){
        report(value, ChangeType.NEW_VALUE);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param change Relationship between newly supplied value and previously reported values.
     */
    public final void report(final double value, final ChangeType change){
        report(value, change, Collections.<String, String>emptyMap());
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param change Relationship between newly supplied value and previously reported values.
     * @param userData Additional data associated with measurement.
     */
    public void report(final double value, final ChangeType change, final Map<String, String> userData){
        final FloatingPointMeasurement measurement = new FloatingPointMeasurement(value);
        measurement.setAnnotations(userData);
        measurement.setChangeType(change);
        report(measurement);
    }

    public RuntimeScope scheduleReporting(final ReportingTask<? super FloatingPointMeasurementReporter> task, final long delay, final TimeUnit unit){
        return scheduleReporting(createTask(this, task), delay, unit);
    }
}
