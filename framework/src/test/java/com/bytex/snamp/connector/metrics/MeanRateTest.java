package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

/**
 * Represents test for {@link MeanRate}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MeanRateTest extends Assert {
    @Test
    public void meanRateTest() throws InterruptedException {
        final MeanRate rate = new MeanRate(Duration.ofMillis(300), Duration.ofMillis(100));
        rate.mark();
        rate.mark();
        rate.mark();
        Thread.sleep(100);
        rate.mark();
        rate.mark();
        rate.mark();
        Thread.sleep(100);
        rate.mark();
        rate.mark();
        Thread.sleep(100);
        assertEquals(2.6D, rate.getAsDouble(), 0.1D);
    }
}
