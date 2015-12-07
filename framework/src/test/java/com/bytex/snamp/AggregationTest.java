package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 */
public final class AggregationTest extends Assert {
    private static final class TestAggregator extends AbstractAggregator {

        @Aggregation
        @SpecialUse
        private final StringBuilder service1 = new StringBuilder("Hello, world!");

        @Aggregation
        @SpecialUse
        public Short[] getService2(){
            return new Short[]{1, 2, 3};
        }
    }


    @Test
    public void serviceRetrievingTest(){
        final TestAggregator provider = new TestAggregator();
        assertTrue(provider.queryObject(StringBuilder.class) != null);
        assertTrue(provider.queryObject(Short[].class) != null);
    }

    @Test
    public void inlineAggregationTest(){
        final Aggregator aggregator = AbstractAggregator.builder()
                .aggregate(CharSequence.class, "Frank Underwood")
                .aggregate(int[].class, new int[]{42, 43})
                .build();
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
    }

    @Test
    public void composeTest(){
        final Aggregator aggregator1 = AbstractAggregator.builder()
                .aggregate(CharSequence.class, "Frank Underwood")
                .aggregate(int[].class, new int[]{42, 43})
                .build();
        final Aggregator aggregator2 = AbstractAggregator.builder()
                .aggregate(Long.class, 56L)
                .aggregate(Boolean.class, Boolean.TRUE)
                .build();
        final Aggregator aggregator = AbstractAggregator.compose(aggregator1, aggregator2);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class));
    }
}
