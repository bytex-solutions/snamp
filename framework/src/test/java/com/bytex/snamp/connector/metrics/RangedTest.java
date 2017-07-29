package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Represents tests for {@link Ranged} implementors.
 * @author Roman Sakno
 * @version 2.1
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
}
