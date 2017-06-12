package com.bytex.snamp.gateway.nagios;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NagiosThresholdTest extends Assert {
    @Test
    public void rangeTest(){
        NagiosThreshold threshold = new NagiosThreshold("10");
        assertTrue(threshold.test(7));
        assertTrue(threshold.test(0));
        assertTrue(threshold.test(10));
        assertFalse(threshold.test(-1));
        assertFalse(threshold.test(11));

        threshold = new NagiosThreshold("10:");
        assertFalse(threshold.test(7));
        assertTrue(threshold.test(11));
        assertTrue(threshold.test(10));

        threshold = new NagiosThreshold("~:10");
        assertTrue(threshold.test(0));
        assertTrue(threshold.test(7));
        assertTrue(threshold.test(10));
        assertFalse(threshold.test(11));

        threshold = new NagiosThreshold("10:20");
        assertTrue(threshold.test(11));
        assertFalse(threshold.test(9));
        assertFalse(threshold.test(21));
        assertTrue(threshold.test(10));
        assertTrue(threshold.test(20));
    }

    @Test
    public void inverseRangeTest(){
        NagiosThreshold threshold = new NagiosThreshold("@10");
        assertFalse(threshold.test(7));
        assertFalse(threshold.test(0));
        assertFalse(threshold.test(10));
        assertTrue(threshold.test(-1));
        assertTrue(threshold.test(11));

        threshold = new NagiosThreshold("@10:");
        assertTrue(threshold.test(7));
        assertFalse(threshold.test(11));
        assertFalse(threshold.test(10));

        threshold = new NagiosThreshold("@~:10");
        assertFalse(threshold.test(0));
        assertFalse(threshold.test(7));
        assertFalse(threshold.test(10));
        assertTrue(threshold.test(11));

        threshold = new NagiosThreshold("@10:20");
        assertFalse(threshold.test(11));
        assertTrue(threshold.test(9));
        assertTrue(threshold.test(21));
        assertFalse(threshold.test(10));
        assertFalse(threshold.test(20));
    }
}
