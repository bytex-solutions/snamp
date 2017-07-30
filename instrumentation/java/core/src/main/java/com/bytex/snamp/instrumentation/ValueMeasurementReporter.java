package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.ValueMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Map;

/**
 * Represents reporter for all instant measurements.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
abstract class ValueMeasurementReporter<M extends ValueMeasurement> extends MeasurementReporter<M> {
    ValueMeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }
}
