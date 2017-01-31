package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
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
        assertEquals(2, recorder.getTotalRate());
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
        assertEquals(0.93D, recorder.getCorrelation(), 0.01D);
    }

    @Test
    public void availabilityTest() throws InterruptedException {
        final ArrivalsRecorder recorder = new ArrivalsRecorder("testGauge");
        recorder.setChannels(1);
        recorder.accept(Duration.ofSeconds(1L));
        recorder.accept(Duration.ofSeconds(1L));
        recorder.accept(Duration.ofSeconds(2L));
        Thread.sleep(1001);
        final double avail = recorder.getLastMeanAvailability(MetricsInterval.SECOND) * 100;
        assertTrue(avail > 20D);
        assertTrue(avail < 25D);
    }

    @Test
    public void serializationTest() throws IOException {
        ArrivalsRecorder recorder = new ArrivalsRecorder("testGauge");
        recorder.accept(Duration.ofSeconds(1L));
        recorder.accept(Duration.ofSeconds(1L));
        final byte[] serializationData = IOUtils.serialize(recorder);
        recorder = IOUtils.deserialize(serializationData, ArrivalsRecorder.class);
        assertEquals(2, recorder.getTotalRate());
    }
}
