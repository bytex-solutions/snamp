package com.bytex.snamp.moa;

import com.bytex.snamp.parser.ParseException;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DoubleRangeParserTest extends Assert {
    @Test
    public void singletonRange() throws ParseException {
        final Range<Double> expected = Range.singleton(42D);
        Range<Double> actual = DoubleRangeParser.parse("42");
        assertEquals(expected, actual);
        actual = DoubleRangeParser.parse(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void positiveInfinityTest() throws ParseException{
        Range<Double> expected = Range.downTo(42D, BoundType.OPEN);
        Range<Double> actual = DoubleRangeParser.parse("(42.0‥+∞)");
        assertEquals(expected, actual);
        actual = DoubleRangeParser.parse(expected.toString());
        assertEquals(expected, actual);
        expected = Range.downTo(42D, BoundType.CLOSED);
        actual = DoubleRangeParser.parse("[42.0‥+∞)");
        assertEquals(expected, actual);
        actual = DoubleRangeParser.parse(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void negativeInfinityTest() throws ParseException{
        Range<Double> expected = Range.upTo(42D, BoundType.OPEN);
        Range<Double> actual = DoubleRangeParser.parse("(-∞‥42.0)");
        assertEquals(expected, actual);
        actual = DoubleRangeParser.parse(expected.toString());
        assertEquals(expected, actual);
        expected = Range.upTo(42D, BoundType.CLOSED);
        actual = DoubleRangeParser.parse("(-∞‥42.0]");
        assertEquals(expected, actual);
        actual = DoubleRangeParser.parse(expected.toString());
        assertEquals(expected, actual);
    }

    @Test
    public void allTest() throws ParseException{
        final Range<Double> expected = Range.all();
        Range<Double> actual = DoubleRangeParser.parse("(-∞‥+∞)");
        assertEquals(expected, actual);
        actual = DoubleRangeParser.parse(expected.toString());
        assertEquals(expected, actual);
    }
}
