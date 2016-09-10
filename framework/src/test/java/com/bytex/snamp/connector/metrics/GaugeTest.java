package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Random;

/**
 * Represents tests for {@link StringGaugeRecorder}, {@link Gauge64} and {@link GaugeFP}
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GaugeTest extends Assert {
    @Test
    public void gaugeFpTest(){
        final GaugeFPRecorder writer = new GaugeFPRecorder("testGauge");
        writer.accept(10D);
        writer.accept(20D);
        writer.accept(30D);
        writer.accept(5D);
        writer.accept(15D);
        writer.accept(16D);
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
        for(int i = 0; i < 100000; i++)
            writer.accept(rnd.nextDouble());
        System.out.println(Duration.ofNanos(System.nanoTime() - nanos));
        System.out.println(writer.getMaxValue());
        System.out.println(writer.getMinValue());
    }

    @Test
    public void gauge64Test(){
        final Gauge64Recorder writer = new Gauge64Recorder("testGauge");
        writer.accept(10L);
        writer.accept(20L);
        writer.accept(30L);
        writer.accept(5L);
        writer.accept(15L);
        writer.accept(16L);
        assertEquals(30L, writer.getMaxValue());
        assertEquals(5L, writer.getMinValue());
        assertEquals(16L, writer.getLastValue());
        assertEquals(19.6D, writer.getQuantile(0.7), 0.1D);
    }

    @Test
    public void test(){
        double i = Double.POSITIVE_INFINITY;
        System.out.println(1 - i);
    }

    @Test
    public void stringGaugeTest(){
        final StringGaugeRecorder writer = new StringGaugeRecorder("testGauge");
        writer.accept("a");
        writer.accept("b");
        writer.accept("ab");
        assertEquals("ab", writer.getLastValue());
        assertEquals("b", writer.getMaxValue());
        assertEquals("a", writer.getMinValue());
    }
}
