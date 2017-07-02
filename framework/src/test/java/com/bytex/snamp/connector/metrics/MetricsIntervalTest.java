package com.bytex.snamp.connector.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.util.SortedSet;

/**
 * Provides test for {@link MetricsInterval}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MetricsIntervalTest extends Assert {
    @Test
    public void nextTest(){
        assertEquals(MetricsInterval.DAY, MetricsInterval.HALF_DAY.next());
        assertEquals(MetricsInterval.MINUTE, MetricsInterval.SECOND.next());
        assertNull(MetricsInterval.DAY.next());
    }

    @Test
    public void allIntervalsTest(){
        assertEquals(MetricsInterval.SECOND, MetricsInterval.ALL_INTERVALS.first());
        assertEquals(MetricsInterval.DAY, MetricsInterval.ALL_INTERVALS.last());
    }

    @Test
    public void greaterTest(){
        SortedSet<MetricsInterval> actual = MetricsInterval.HALF_DAY.greater();
        assertEquals(1, actual.size());
        assertTrue(actual.contains(MetricsInterval.DAY));
        actual = MetricsInterval.DAY.greater();
        assertEquals(0, actual.size());
    }

    @Test
    public void lessThanTest(){
        SortedSet<MetricsInterval> actual = MetricsInterval.MINUTE.less();
        assertEquals(1, actual.size());
        assertTrue(actual.contains(MetricsInterval.SECOND));
        actual = MetricsInterval.SECOND.less();
        assertEquals(0, actual.size());
    }
}
