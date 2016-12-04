package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.reporters.InMemoryReporter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MetricRegistryTest extends Assert {
    @Test
    public void integerTest() throws IOException {
        final InMemoryReporter reporter = new InMemoryReporter();
        final MetricRegistry registry = new MetricRegistry(reporter);
        final String NAME = "testInt";
        final IntegerMeasurementReporter integer = registry.integer(NAME);
        assertEquals(integer, registry.integer(NAME));
        integer.report(42L);
        integer.report(43L);
        assertEquals(2L, reporter.size());
        registry.close();
    }

    @Test
    public void timerTest() throws IOException, InterruptedException {
        final InMemoryReporter reporter = new InMemoryReporter();
        final MetricRegistry registry = new MetricRegistry(reporter);
        final String NAME = "testTimer";
        final TimeMeasurementReporter timer = registry.timer(NAME);
        assertEquals(timer, registry.timer(NAME));
        final MeasurementScope scope = timer.start();
        try {
            Thread.sleep(1000);
        } finally {
            scope.close();
        }
        assertEquals(1L, reporter.size());
    }
}
