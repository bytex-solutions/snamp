package com.bytex.snamp.moa;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RangeUtilsTest extends Assert {
    @Test
    public void locationTest(){
        Range<Integer> range = Range.range(0, BoundType.OPEN, 10, BoundType.CLOSED);
        assertEquals(0, RangeUtils.getLocation(1, range));
        assertEquals(0, RangeUtils.getLocation(10, range));
        assertEquals(-1, RangeUtils.getLocation(0, range));
        assertEquals(-1, RangeUtils.getLocation(-100, range));
        assertEquals(1, RangeUtils.getLocation(11, range));

        range = Range.range(0, BoundType.CLOSED, 10, BoundType.OPEN);
        assertEquals(0, RangeUtils.getLocation(1, range));
        assertEquals(1, RangeUtils.getLocation(10, range));
        assertEquals(0, RangeUtils.getLocation(0, range));
        assertEquals(-1, RangeUtils.getLocation(-100, range));
        assertEquals(1, RangeUtils.getLocation(11, range));

        range = Range.downTo(10, BoundType.CLOSED);
        assertEquals(0, RangeUtils.getLocation(10, range));
        assertEquals(-1, RangeUtils.getLocation(9, range));
        assertEquals(0, RangeUtils.getLocation(Integer.MAX_VALUE, range));

        range = Range.all();
        assertEquals(0, RangeUtils.getLocation(10, range));
        assertEquals(0, RangeUtils.getLocation(9, range));
        assertEquals(0, RangeUtils.getLocation(Integer.MAX_VALUE, range));

    }

    @Test
    public void singletonRange() {
        final Range<Double> expected = Range.singleton(42D);
        Range<Double> actual = RangeUtils.parseDoubleRange("42");
        assertEquals(expected, actual);
        actual = RangeUtils.parseDoubleRange(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void positiveInfinityTest() {
        Range<Double> expected = Range.downTo(42D, BoundType.OPEN);
        Range<Double> actual = RangeUtils.parseDoubleRange("(42.0‥+∞)");
        assertEquals(expected, actual);
        actual = RangeUtils.parseDoubleRange(expected.toString());
        assertEquals(expected, actual);
        expected = Range.downTo(42D, BoundType.CLOSED);
        actual = RangeUtils.parseDoubleRange("[42.0‥+∞)");
        assertEquals(expected, actual);
        actual = RangeUtils.parseDoubleRange(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void negativeInfinityTest() {
        Range<Double> expected = Range.upTo(42D, BoundType.OPEN);
        Range<Double> actual = RangeUtils.parseDoubleRange("(-∞‥42.0)");
        assertEquals(expected, actual);
        actual = RangeUtils.parseDoubleRange(expected.toString());
        assertEquals(expected, actual);
        expected = Range.upTo(42D, BoundType.CLOSED);
        actual = RangeUtils.parseDoubleRange("(-∞‥42.0]");
        assertEquals(expected, actual);
        actual = RangeUtils.parseDoubleRange(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void allTest(){
        final Range<Double> expected = Range.all();
        Range<Double> actual = RangeUtils.parseDoubleRange("(-∞‥+∞)");
        assertEquals(expected, actual);
        actual = RangeUtils.parseDoubleRange(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void emptyRangeTest(){
        final Range<Double> range = RangeUtils.EMPTY_DOUBLE_RANGE;
        assertTrue(range.isEmpty());
    }
}
