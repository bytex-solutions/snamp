package com.bytex.snamp.jmx;

import com.bytex.snamp.connector.metrics.*;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.CompositeData;

import static com.bytex.snamp.jmx.CompositeDataUtils.getDouble;
import static com.bytex.snamp.jmx.CompositeDataUtils.getLong;
import static com.bytex.snamp.jmx.CompositeDataUtils.getString;

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
}
