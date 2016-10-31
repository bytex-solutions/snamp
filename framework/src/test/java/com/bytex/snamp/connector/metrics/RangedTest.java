package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.jmx.MetricsConverter;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import java.time.Duration;

import static com.bytex.snamp.jmx.CompositeDataUtils.getDouble;

/**
 * Represents tests for {@link Ranged} implementors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class RangedTest extends Assert {
    @Test
    public void rangedTimer(){
        final RangedTimerRecorder recorder = new RangedTimerRecorder("testTimer", Duration.ofMillis(100), Duration.ofMillis(200));
        recorder.accept(Duration.ofMillis(10));
        recorder.accept(Duration.ofMillis(20));
        recorder.accept(Duration.ofMillis(220));
        assertEquals(0.33D, recorder.getPercentOfGreaterThanRange(), 0.01D);
        assertEquals(0.66D, recorder.getPercentOfLessThanRange(), 0.01D);
        assertEquals(0D, recorder.getPercentOfValuesIsInRange(), 0.01D);
        assertEquals(0.3D, recorder.getPercentOfGreaterThanRange(MetricsInterval.MINUTE), 0.1D);
        assertEquals(0.6D, recorder.getPercentOfLessThanRange(MetricsInterval.MINUTE), 0.1D);
        assertEquals(0D, recorder.getPercentOfValuesIsInRange(MetricsInterval.MINUTE), 0.1D);
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
}
