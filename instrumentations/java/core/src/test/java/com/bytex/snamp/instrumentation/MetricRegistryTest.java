package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.reporters.InMemoryReporter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MetricRegistryTest extends Assert {
    @Test
    public void integerTest() throws IOException, InterruptedException {
        final InMemoryReporter reporter = new InMemoryReporter();
        final MetricRegistry registry = new MetricRegistry(reporter);
        final String NAME = "testInt";
        final IntegerMeasurementReporter integer = registry.integer(NAME);
        assertEquals(integer, registry.integer(NAME));
        integer.report(42L);
        integer.report(43L);
        assertEquals(2L, reporter.size());
        reporter.clear();
        //schedule reporting
        final MeasurementScope scope = registry.integer(NAME).scheduleReporting(new MeasurementReporter.ReportingTask<IntegerMeasurementReporter>() {
            @Override
            public boolean report(final IntegerMeasurementReporter reporter) {
                reporter.report(50L);
                return true;
            }
        }, 100, TimeUnit.MILLISECONDS);
        Thread.sleep(1001L);
        scope.close();
        assertTrue(reporter.size() >= 9);
    }

    @Test
    public void booleanTest() throws IOException {
        final InMemoryReporter reporter = new InMemoryReporter();
        final MetricRegistry registry = new MetricRegistry(reporter);
        final String NAME = "testBool";
        registry.bool(NAME).report(true);
        registry.bool(NAME).report(true);
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

    @Test
    public void traceInstrumentationTest(){
        final InMemoryReporter reporter = new InMemoryReporter();
        final MetricRegistry registry = new MetricRegistry(reporter);
        final String NAME = "tracer";
        final CharSequence sequence = (CharSequence) registry.tracer(NAME).wrap(new StringBuilder("value"), CharSequence.class);
        assertEquals(5, sequence.length());
        assertEquals(1, reporter.size());
        reporter.clear();
        assertEquals(0, reporter.size());
        registry.tracer(NAME).wrap(new Runnable() {
            @Override
            public void run() {

            }
        }).run();
        assertEquals(1, reporter.size());
    }

    @Test
    public void traceTest(){
        final InMemoryReporter reporter = new InMemoryReporter();
        final MetricRegistry registry = new MetricRegistry(reporter);
        final String NAME = "tracer";
        final TraceScope scope = registry.tracer(NAME).beginTrace();
        assertEquals(scope, TraceScope.current());
        assertNull(TraceScope.current().getParent());
        assertNull(scope.getParent());
        //push another scope
        registry.tracer(NAME).beginTrace();
        assertEquals(scope, TraceScope.current().getParent());
        TraceScope.current().close();
        assertEquals(scope, TraceScope.current());
        scope.close();
        assertNull(TraceScope.current());
    }
}
