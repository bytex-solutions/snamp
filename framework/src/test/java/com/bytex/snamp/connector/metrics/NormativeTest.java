package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Represents tests for {@link Normative} implementors.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class NormativeTest extends Assert {
    @Test
    public void timingNormativeTest(){
        final TimerWithNormativeRecorder recorder = new TimerWithNormativeRecorder("testTimer", Duration.ofMillis(100), Duration.ofMillis(200));
        recorder.accept(Duration.ofMillis(10));
        recorder.accept(Duration.ofMillis(20));
        recorder.accept(Duration.ofMillis(220));
        assertEquals(1, recorder.getCountOfGreaterThanNormative());
        assertEquals(2, recorder.getCountOfLessThanNormative());
        assertEquals(0, recorder.getCountOfNormalValues());
    }
}
