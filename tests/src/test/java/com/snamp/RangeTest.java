package com.snamp;

import org.junit.Test;

/**
 * @author roman
 */
public final class RangeTest extends SnampClassTestSet<Range<?>> {
    @Test
    public final void isInInt32RangeTest(){
        final Range<Integer> r = new Range<Integer>(10, 20);
        assertTrue(r.contains(10, Range.InclusionTestType.FULL_INCLUSIVE));
        assertTrue(r.contains(10, Range.InclusionTestType.INCLUDE_LOWER_BOUND_EXCLUDE_UPPER_BOUND));
        assertFalse(r.contains(10, Range.InclusionTestType.FULL_EXCLUSIVE));
        assertFalse(r.contains(10, Range.InclusionTestType.EXCLUDE_LOWER_BOUND_INCLUDE_UPPER_BOUND));
    }
}
