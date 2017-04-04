package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.TimeMeasurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
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

    /**
     * Reports about new value of this metric.
     * @param nanos Time span, in nanoseconds.
     */
    public final void report(final long nanos){
        report(nanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Reports about new value of this metric.
     * @param duration Time span.
     * @param unit Unit of time measurement.
     */
    public final void report(final long duration, final TimeUnit unit){
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
        measurement.setAnnotations(userData);
        report(measurement);
    }

    /**
     * Starts timer.
     * @param userData Additional data associated with time measurement.
     * @return An object used to stop timer
     */
    public RuntimeScope start(final Map<String, String> userData){
        return new RuntimeScope() {
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
    public final RuntimeScope start(){
        return start(Collections.<String, String>emptyMap());
    }

    public <V> V call(final Callable<V> callable, final Map<String, String> userData) throws Exception {
        try (final RuntimeScope scope = start(userData)) {
            return callable.call();
        }
    }

    public final <V> V call(final Callable<V> callable) throws Exception{
        return call(callable, Collections.<String, String>emptyMap());
    }

    public void run(final Runnable runnable, final Map<String, String> userData) {
        try (final RuntimeScope scope = start(userData)) {
            runnable.run();
        }
    }

    public final void run(final Runnable runnable){
        run(runnable, Collections.<String, String>emptyMap());
    }

    public RuntimeScope scheduleReporting(final ReportingTask<? super TimeMeasurementReporter> task, final long delay, final TimeUnit unit){
        return scheduleReporting(createTask(this, task), delay, unit);
    }
}
