package com.itworks.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AccumulatorTest extends Assert {

    @Test
    public void sumAccumulator() throws InterruptedException {
        final SumLongAccumulator acc = new SumLongAccumulator(5L, 500L);
        assertEquals(15L, acc.setAndGet(10L));
        assertEquals(20L, acc.setAndGet(5L));
        Thread.sleep(500);
        assertEquals(5L, acc.setAndGet(5L));
    }

    @Test
    public void peakAccumulator() throws InterruptedException{
        final PeakLongAccumulator acc = new PeakLongAccumulator(0L, 500L);
        assertEquals(10L, acc.setAndGet(10L));
        assertEquals(10L, acc.setAndGet(5L));
        Thread.sleep(500);
        assertEquals(5L, acc.setAndGet(5L));
    }
}
