package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.ChangeType;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents reporter for measurement of type {@code long}.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public class IntegerMeasurementReporter extends ValueMeasurementReporter<IntegerMeasurement> {
    protected IntegerMeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     */
    public final void report(final long value){
        report(value, ChangeType.NEW_VALUE);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param change Relationship between newly supplied value and previously reported values.
     */
    public final void report(final long value, final ChangeType change){
        report(value, change, Collections.<String, String>emptyMap());
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param change Relationship between newly supplied value and previously reported values.
     * @param userData Additional data associated with measurement.
     */
    public void report(final long value, final ChangeType change, final Map<String, String> userData){
        final IntegerMeasurement measurement = new IntegerMeasurement(value);
        measurement.setAnnotations(userData);
        measurement.setChangeType(change);
        report(measurement);
    }

    public RuntimeScope scheduleReporting(final ReportingTask<? super IntegerMeasurementReporter> task, final long delay, final TimeUnit unit){
        return scheduleReporting(createTask(this, task), delay, unit);
    }
}
