package com.bytex.snamp.instrumentation.reporters.http;

import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HttpReporterTest extends Assert {
    @Test
    public void failedReportTest() throws URISyntaxException, IOException, InterruptedException {
        final HttpReporter reporter = new HttpReporter("http://localhost:9099", null);
        reporter.report(new IntegerMeasurement(42L));
        Thread.sleep(300);
        assertEquals(1, reporter.getBufferedMeasurements());
        reporter.close();
    }
}
