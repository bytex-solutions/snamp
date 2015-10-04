package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.TimeSpan;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DiffAccumulatorTest extends Assert {
    @Test
    public void longAccumulatorTest() throws InterruptedException {
        final DiffLongAccumulator acc = new DiffLongAccumulator(TimeSpan.ofMillis(500L));
        assertEquals(0L, acc.update(15L));
        assertEquals(4L, acc.update(19L));
        assertEquals(8L, acc.update(23L));
        Thread.sleep(501L);
        assertEquals(0L, acc.update(25L));
        assertEquals(2L, acc.update(27L));
        assertEquals(5L, acc.update(30L));
    }

    @Test
    public void intAccumulatorTest() throws InterruptedException {
        final DiffIntAccumulator acc = new DiffIntAccumulator(TimeSpan.ofMillis(500L));
        assertEquals(0L, acc.update(15));
        assertEquals(4L, acc.update(19));
        assertEquals(8L, acc.update(23));
        Thread.sleep(501L);
        assertEquals(0L, acc.update(25));
        assertEquals(2L, acc.update(27));
        assertEquals(5L, acc.update(30));
    }
}
