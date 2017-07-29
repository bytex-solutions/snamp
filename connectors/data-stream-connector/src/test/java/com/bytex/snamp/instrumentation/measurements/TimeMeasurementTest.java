package com.bytex.snamp.instrumentation.measurements;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class TimeMeasurementTest extends Assert {
    @Test
    public void convertToDurationTest(){
        final TimeMeasurement measurement = new TimeMeasurement();
        measurement.setDuration(200, TimeUnit.MILLISECONDS);
        assertEquals(Duration.ofMillis(200), measurement.convertTo(Duration.class));
    }
}
