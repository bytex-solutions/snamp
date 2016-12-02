package com.bytex.snamp.instrumentation.reporting;

import com.bytex.snamp.instrumentation.IntegerMeasurement;

import java.util.logging.Logger;

/**
 * Represents reporter
 */
public class IntegerMeasurementReporter extends ValueMeasurementReporter<IntegerMeasurement> {
    protected IntegerMeasurementReporter(final Iterable<Reporter> reporters, final String name) {
        super(reporters, name);
    }

    public void report(final long value){
        report(new IntegerMeasurement(value));
    }
}
