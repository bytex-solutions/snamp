package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.ChangeType;
import com.bytex.snamp.instrumentation.measurements.StringMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents reporter for measurement of type {@link String}.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class StringMeasurementReporter extends ValueMeasurementReporter<StringMeasurement> {
    protected StringMeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param change Relationship between newly supplied value and previously reported values.
     * @param userData Additional data associated with measurement.
     */
    public void report(final String value, final ChangeType change, final Map<String, String> userData){
        final StringMeasurement measurement = new StringMeasurement();
        measurement.setAnnotations(userData);
        measurement.setChangeType(change);
        report(measurement);
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     * @param change Relationship between newly supplied value and previously reported values.
     */
    public final void report(final String value, final ChangeType change){
        report(value, change, Collections.<String, String>emptyMap());
    }

    /**
     * Reports about new value of this metric.
     * @param value A new value of this metric.
     */
    public final void report(final String value){
        report(value, ChangeType.NEW_VALUE);
    }

    public RuntimeScope scheduleReporting(final ReportingTask<? super StringMeasurementReporter> task, final long delay, final TimeUnit unit){
        return scheduleReporting(createTask(this, task), delay, unit);
    }
}
