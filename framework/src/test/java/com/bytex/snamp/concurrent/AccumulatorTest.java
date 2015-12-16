package com.bytex.snamp.concurrent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AccumulatorTest extends Assert {

    @Test
    public void sumLongAccumulator() throws InterruptedException {
        final LongAccumulator acc = LongAccumulator.adder(5L, 500L);
        assertEquals(15L, acc.update(10L));
        assertEquals(20L, acc.update(5L));
        Thread.sleep(501L);
        assertEquals(10L, acc.update(5L));
    }

    @Test
    public void sumIntAccumulator() throws InterruptedException {
        final IntAccumulator acc = IntAccumulator.adder(5, 500L);
        assertEquals(15, acc.update(10));
        assertEquals(20, acc.update(5));
        Thread.sleep(501L);
        assertEquals(10, acc.update(5));
    }

    @Test
    public void peakLongAccumulator() throws InterruptedException{
        final LongAccumulator acc = LongAccumulator.peak(0L, 500L);
        assertEquals(10L, acc.update(10L));
        assertEquals(10L, acc.update(5L));
        Thread.sleep(501L);
        assertEquals(5L, acc.update(5L));
    }

    @Test
    public void peakIntAccumulator() throws InterruptedException{
        final IntAccumulator acc = IntAccumulator.peak(0, 500L);
        assertEquals(10L, acc.update(10));
        assertEquals(10L, acc.update(5));
        Thread.sleep(501L);
        assertEquals(5L, acc.update(5));
    }
}
