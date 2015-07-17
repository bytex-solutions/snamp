package com.itworks.snamp.connectors.modbus;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link IntegerRange}
 */
public final class IntegerRangeTest extends Assert {
    @Test
    public void parsingTest(){
        final IntegerRange range = new IntegerRange("2..10");
        assertEquals(2, range.getLowerBound());
        assertEquals(10, range.getUpperBound());
        assertEquals(9, range.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidParsingTest(){
        final IntegerRange range = new IntegerRange("12..14a");
    }
}
