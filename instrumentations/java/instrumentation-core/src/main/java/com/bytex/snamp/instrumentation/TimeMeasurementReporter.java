package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.TimeMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class TimeMeasurementReporter extends MeasurementReporter<TimeMeasurement> {

    protected TimeMeasurementReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    public void report(final long nanos){
        report(nanos, TimeUnit.NANOSECONDS);
    }

    public void report(final long duration, final TimeUnit unit){
        report(duration, unit, Collections.<String, String>emptyMap());
    }

    /**
     * Reports about new value of this metric.
     * @param duration Time span.
     * @param unit Unit of time measurement.
     * @param userData Additional data associated with measurement.
     */
    public void report(final long duration, final TimeUnit unit, final Map<String, String> userData){
        final TimeMeasurement measurement = new TimeMeasurement(duration, unit);
        measurement.setUserData(userData);
        report(measurement);
    }

    /**
     * Starts timer.
     * @param userData Additional data associated with time measurement.
     * @return An object used to stop timer
     */
    public MeasurementScope start(final Map<String, String> userData){
        return new MeasurementScope() {
            private final long startTime = System.nanoTime();

            @Override
            public void close() {
                report(System.nanoTime() - startTime, TimeUnit.NANOSECONDS, userData);
            }
        };
    }

    /**
     * Starts timer.
     * @return An object used to stop timer
     */
    public MeasurementScope start(){
        return start(Collections.<String, String>emptyMap());
    }

    public MeasurementScope scheduleReporting(final ReportingTask<? super TimeMeasurementReporter> task, final long delay, final TimeUnit unit){
        return scheduleReporting(createTask(this, task), delay, unit);
    }
}
