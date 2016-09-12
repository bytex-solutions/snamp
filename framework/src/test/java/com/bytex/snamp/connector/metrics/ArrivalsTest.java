package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Represents test for {@link Arrivals}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ArrivalsTest extends Assert {
    @Test
    public void correlationTest() throws InterruptedException {
        final ArrivalsRecorder recorder = new ArrivalsRecorder("testGauge");
        recorder.accept(Duration.ofSeconds(1L));
        recorder.accept(Duration.ofSeconds(1L));
        assertEquals(2, recorder.getRequestRate().getTotalRate());
        Thread.sleep(1001);//wait for second and push three requests with linearly increased response time
        recorder.accept(Duration.ofSeconds(2L));
        recorder.accept(Duration.ofSeconds(2L));
        recorder.accept(Duration.ofSeconds(2L));
        Thread.sleep(1001);
        recorder.accept(Duration.ofSeconds(3L));
        recorder.accept(Duration.ofSeconds(3L));
        recorder.accept(Duration.ofSeconds(3L));
        recorder.accept(Duration.ofSeconds(3L));
        Thread.sleep(1001);
        System.out.println(recorder.getCorrelation());
    }
}
