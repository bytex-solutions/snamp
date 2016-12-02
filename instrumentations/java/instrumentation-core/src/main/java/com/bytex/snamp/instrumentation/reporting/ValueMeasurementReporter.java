package com.bytex.snamp.instrumentation.reporting;

import com.bytex.snamp.instrumentation.ValueMeasurement;

/**
 *
 */
abstract class ValueMeasurementReporter<M extends ValueMeasurement> extends MeasurementReporter<M> {
    ValueMeasurementReporter(final Iterable<Reporter> reporters, final String name) {
        super(reporters, name);
    }
}
