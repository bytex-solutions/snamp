package com.itworks.snamp.adapters.nagios;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NagiosThresholdTest extends Assert {
    @Test
    public void rangeTest(){
        NagiosThreshold threshold = new NagiosThreshold("10");
        assertTrue(threshold.check(7));
        assertTrue(threshold.check(0));
        assertTrue(threshold.check(10));
        assertFalse(threshold.check(-1));
        assertFalse(threshold.check(11));

        threshold = new NagiosThreshold("10:");
        assertFalse(threshold.check(7));
        assertTrue(threshold.check(11));
        assertTrue(threshold.check(10));

        threshold = new NagiosThreshold("~:10");
        assertTrue(threshold.check(0));
        assertTrue(threshold.check(7));
        assertTrue(threshold.check(10));
        assertFalse(threshold.check(11));

        threshold = new NagiosThreshold("10:20");
        assertTrue(threshold.check(11));
        assertFalse(threshold.check(9));
        assertFalse(threshold.check(21));
        assertTrue(threshold.check(10));
        assertTrue(threshold.check(20));
    }

    @Test
    public void inverseRangeTest(){
        NagiosThreshold threshold = new NagiosThreshold("@10");
        assertFalse(threshold.check(7));
        assertFalse(threshold.check(0));
        assertFalse(threshold.check(10));
        assertTrue(threshold.check(-1));
        assertTrue(threshold.check(11));

        threshold = new NagiosThreshold("@10:");
        assertTrue(threshold.check(7));
        assertFalse(threshold.check(11));
        assertFalse(threshold.check(10));

        threshold = new NagiosThreshold("@~:10");
        assertFalse(threshold.check(0));
        assertFalse(threshold.check(7));
        assertFalse(threshold.check(10));
        assertTrue(threshold.check(11));

        threshold = new NagiosThreshold("@10:20");
        assertFalse(threshold.check(11));
        assertTrue(threshold.check(9));
        assertTrue(threshold.check(21));
        assertFalse(threshold.check(10));
        assertFalse(threshold.check(20));
    }
}
