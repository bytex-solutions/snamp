package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Random;

/**
 * Represents tests for {@link com.bytex.snamp.connector.metrics.StringGauge}, {@link Gauge64} and {@link GaugeFP}
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GaugeTest extends Assert {
    @Test
    public void gaugeFpTest(){
        final GaugeFPRecorder writer = new GaugeFPRecorder("testGauge");
        writer.update(10D);
        writer.update(20D);
        writer.update(30D);
        writer.update(5D);
        writer.update(15D);
        writer.update(16D);
        assertEquals(30D, writer.getMaxValue(), 0.1D);
        assertEquals(5D, writer.getMinValue(), 0.1D);
        assertEquals(16D, writer.getLastValue(), 0.1D);
        assertEquals(19.6D, writer.getQuantile(0.7), 0.1D);
    }

    @Test
    public void gaugeFpLoadTest(){
        final GaugeFPRecorder writer = new GaugeFPRecorder("testGauge");
        final Random rnd = new Random(42L);
        final long nanos = System.nanoTime();
        for(int i = 0; i < 10000; i++)
            writer.update(rnd.nextDouble());
        System.out.println(Duration.ofNanos(System.nanoTime() - nanos));
        System.out.println(writer.getMaxValue());
        System.out.println(writer.getMinValue());
    }
}
