package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.Health;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Represents health check reporter.
 */
public class HealthCheckReporter extends MeasurementReporter<Health> {
    protected HealthCheckReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    public final void up(){
        final Health health = new Health();
        health.setStatus(Health.Status.UP);
        report(health);
    }

    public final void down(final Throwable e) {
        final Health health = new Health();
        health.setStatus(Health.Status.DOWN);
        health.setDescription(e.getMessage());
        health.addAnnotation("exception", e.toString());
        final StringWriter writer = new StringWriter(1024);
        try (final PrintWriter printer = new PrintWriter(writer, false)) {
            e.printStackTrace(printer);
            printer.flush();
        }
        health.addAnnotation("stackTrace", writer.toString());
        report(health);
    }

    public final void outOfService() {
        final Health health = new Health();
        health.setStatus(Health.Status.OUT_OF_SERVICE);
        report(health);
    }
}
