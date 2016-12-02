package com.bytex.snamp.instrumentation.reporting;

import com.bytex.snamp.instrumentation.Measurement;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents abstract reporter
 */
abstract class MeasurementReporter<M extends Measurement> {
    private static final Logger LOGGER = Logger.getLogger("SnampMeasurementsReporter");
    private final Iterable<Reporter> reporters;
    private final String name;

    MeasurementReporter(final Iterable<Reporter> reporters, final String name){
        this.reporters = reporters;
        this.name = name;
    }

    final String getName(){
        return name;
    }

    final void report(final M measurement) {
        measurement.setName(name);
        for (final Reporter reporter : reporters)
            try {
                reporter.report(measurement);
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, String.format("Failed to report measurement %s via %s", measurement, reporter));
            }
    }
}
