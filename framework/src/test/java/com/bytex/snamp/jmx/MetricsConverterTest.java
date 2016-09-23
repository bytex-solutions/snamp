package com.bytex.snamp.jmx;

import com.bytex.snamp.connector.metrics.GaugeFPRecorder;
import com.bytex.snamp.connector.metrics.RateRecorder;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.jmx.CompositeDataUtils.getDouble;
import static com.bytex.snamp.jmx.CompositeDataUtils.getLong;

/**
 * Represents tests for {@link MetricsConverter}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricsConverterTest extends Assert {
    @Test
    public void gaugeFPConversion() throws OpenDataException {
        final GaugeFPRecorder gaugeFP = new GaugeFPRecorder("testGauge", 512);
        gaugeFP.accept(12D);
        gaugeFP.accept(64D);
        final CompositeData data = MetricsConverter.fromGaugeFP(gaugeFP);
        assertNotNull(data);
        assertEquals(64D, getDouble(data, "maxValue", Double.NaN), 0.1D);
        assertEquals(12D, getDouble(data, "minValue", Double.NaN), 0.1D);
    }

    @Test
    public void rateConversion() throws OpenDataException, InterruptedException {
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
}
