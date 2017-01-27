package com.bytex.snamp.jmx;

import com.bytex.snamp.connector.metrics.*;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import java.time.Duration;

import static com.bytex.snamp.jmx.CompositeDataUtils.*;

/**
 * Represents tests for {@link MetricsConverter}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricsConverterTest extends Assert {
    @Test
    public void gaugeFPConversion() {
        final GaugeFPRecorder gaugeFP = new GaugeFPRecorder("testGauge", 512);
        gaugeFP.accept(12D);
        gaugeFP.accept(64D);
        final CompositeData data = MetricsConverter.fromGaugeFP(gaugeFP);
        assertNotNull(data);
        assertEquals(64D, getDouble(data, "maxValue", Double.NaN), 0.1D);
        assertEquals(12D, getDouble(data, "minValue", Double.NaN), 0.1D);
    }

    @Test
    public void rateConversion() throws InterruptedException {
        final RateRecorder rate = new RateRecorder("testGauge");
        rate.mark();
        rate.mark();
        Thread.sleep(1001);
        final CompositeData data = MetricsConverter.fromRate(rate);
        assertNotNull(data);
        assertEquals(2L, getLong(data, "totalRate", 0L));
        assertEquals(2D, getDouble(data, "meanRateLastSecond", Double.NaN), 0.1D);
        assertEquals(2D, getDouble(data, "meanRateLastMinute", Double.NaN), 0.1D);
    }

    @Test
    public void ratedGaugeFPConversion() throws InterruptedException{
        final RatedGaugeFPRecorder recorder = new RatedGaugeFPRecorder("testGauge");
        recorder.accept(12D);
        recorder.accept(64D);
        Thread.sleep(1001);
        final CompositeData data = MetricsConverter.fromRatedGaugeFP(recorder);
        assertNotNull(data);
        assertEquals(2L, getLong(data, "totalRate", 0L));
        assertEquals(2D, getDouble(data, "meanRateLastSecond", Double.NaN), 0.1D);
        assertEquals(2D, getDouble(data, "meanRateLastMinute", Double.NaN), 0.1D);
        assertEquals(64D, getDouble(data, "maxValue", Double.NaN), 0.1D);
        assertEquals(12D, getDouble(data, "minValue", Double.NaN), 0.1D);
    }

    @Test
    public void gauge64Conversion(){
        final Gauge64Recorder recorder = new Gauge64Recorder("testGauge");
        recorder.accept(12L);
        recorder.accept(64L);
        final CompositeData data = MetricsConverter.fromGauge64(recorder);
        assertNotNull(data);
        assertEquals(64L, getLong(data, "maxValue", 0L));
        assertEquals(12L, getLong(data, "minValue", 0L));
    }

    @Test
    public void ratedGauge64Conversion() throws InterruptedException{
        final RatedGauge64Recorder recorder = new RatedGauge64Recorder("testGauge");
        recorder.accept(12L);
        recorder.accept(64L);
        Thread.sleep(1001);
        final CompositeData data = MetricsConverter.fromRatedGauge64(recorder);
        assertNotNull(data);
        assertEquals(2L, getLong(data, "totalRate", 0L));
        assertEquals(2D, getDouble(data, "meanRateLastSecond", Double.NaN), 0.1D);
        assertEquals(2D, getDouble(data, "meanRateLastMinute", Double.NaN), 0.1D);
        assertEquals(64L, getLong(data, "maxValue", 0L));
        assertEquals(12L, getLong(data, "minValue", 0L));
    }

    @Test
    public void flagConversion(){
        final FlagRecorder recorder = new FlagRecorder("testGauge");
        recorder.accept(true);
        recorder.accept(true);
        recorder.accept(false);
        final CompositeData data = MetricsConverter.fromFlag(recorder);
        assertNotNull(data);
        assertEquals(2, getLong(data, "totalCountOfTrueValues", 0L));
        assertEquals(1, getLong(data, "totalCountOfFalseValues", 0L));
        assertEquals(2D, getDouble(data, "ratio", Double.NaN), 0.1D);
    }

    @Test
    public void ratedFlagConversion(){
        final RatedFlagRecorder recorder = new RatedFlagRecorder("testGauge");
        recorder.accept(true);
        recorder.accept(true);
        recorder.accept(false);
        final CompositeData data = MetricsConverter.fromRatedFlag(recorder);
        assertNotNull(data);
        assertEquals(2, getLong(data, "totalCountOfTrueValues", 0L));
        assertEquals(1, getLong(data, "totalCountOfFalseValues", 0L));
        assertEquals(2D, getDouble(data, "ratio", Double.NaN), 0.1D);
        assertEquals(3L, getLong(data, "totalRate", 0L));
    }

    @Test
    public void ratedStringGaugeConversion(){
        final RatedStringGaugeRecorder recorder = new RatedStringGaugeRecorder("testGauge");
        recorder.accept("3");
        recorder.accept("1");
        recorder.accept("2");
        final CompositeData data = MetricsConverter.fromRatedStringGauge(recorder);
        assertNotNull(data);
        assertEquals("3", getString(data, "maxValue", ""));
        assertEquals("1", getString(data, "minValue", ""));
        assertEquals("2", getString(data, "lastValue", ""));
    }

    @Test
    public void ratedTimerConversion(){
        final RatedTimeRecorder recorder = new RatedTimeRecorder("testTimer");
        recorder.accept(Duration.ofMillis(10));
        recorder.accept(Duration.ofMillis(50));
        recorder.accept(Duration.ofMillis(30));
        assertEquals(Duration.ofMillis(30), recorder.getMeanValue());
        final CompositeData data = MetricsConverter.fromRatedTimer(recorder);
        assertNotNull(data);
        assertEquals(0.01D, getDouble(data, "minValue", 0D), 0.001D);
        assertEquals(0.05D, getDouble(data, "maxValue", 0D), 0.001D);
        assertEquals(0.03D, getDouble(data, "meanValue", 0D), 0.001D);
    }

    @Test
    public void rangedTimerConversion(){
        final RangedTimerRecorder recorder = new RangedTimerRecorder("testTimer", Duration.ofMillis(100), Duration.ofMillis(200));
        recorder.accept(Duration.ofMillis(10));
        recorder.accept(Duration.ofMillis(20));
        recorder.accept(Duration.ofMillis(220));
        final CompositeData data = MetricsConverter.fromRangedTimer(recorder);
        assertNotNull(data);
        assertEquals(0.66D, getDouble(data, "lessThanRange", Double.NaN), 0.01D);
        assertEquals(0D, getDouble(data, "isInRange", Double.NaN), 0.01D);
        assertEquals(0.33D, getDouble(data, "greaterThanRange", Double.NaN), 0.01D);
    }

    @Test
    public void rangedGauge64Conversion(){
        final RangedGauge64Recorder recorder = new RangedGauge64Recorder("testGauge", 10L, 20L);
        recorder.accept(5L);
        recorder.accept(3L);
        recorder.accept(4L);
        recorder.accept(10L);
        recorder.accept(21L);
        final CompositeData data = MetricsConverter.fromRanged64(recorder);
        assertNotNull(data);
        assertEquals(3D/5D, getDouble(data, "lessThanRange", Double.NaN), 0.01D);
        assertEquals(1D/5D, getDouble(data, "isInRange", Double.NaN), 0.01D);
        assertEquals(1D/5D, getDouble(data, "greaterThanRange", Double.NaN), 0.01D);
    }

    @Test
    public void rangedGaugeFPConversion(){
        final RangedGaugeFPRecorder recorder = new RangedGaugeFPRecorder("testGauge", 10L, 20L);
        recorder.accept(5D);
        recorder.accept(3D);
        recorder.accept(4D);
        recorder.accept(10D);
        recorder.accept(21D);
        final CompositeData data = MetricsConverter.fromRangedFP(recorder);
        assertNotNull(data);
        assertEquals(3D/5D, getDouble(data, "lessThanRange", Double.NaN), 0.01D);
        assertEquals(1D/5D, getDouble(data, "isInRange", Double.NaN), 0.01D);
        assertEquals(1D/5D, getDouble(data, "greaterThanRange", Double.NaN), 0.01D);
    }

    @Test
    public void arrivalsConversion() throws InterruptedException {
        final ArrivalsRecorder recorder = new ArrivalsRecorder("testGauge");
        recorder.setChannels(1);
        recorder.accept(Duration.ofSeconds(1L));
        recorder.accept(Duration.ofSeconds(1L));
        recorder.accept(Duration.ofSeconds(2L));
        Thread.sleep(1001);
        final CompositeData data = MetricsConverter.fromArrivals(recorder);
        final double avail = getDouble(data, "meanAvailabilityLastSecond", Double.NaN) * 100;
        assertTrue(avail > 20D);
    }
}
