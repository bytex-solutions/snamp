package com.bytex.snamp;

import com.bytex.snamp.connectors.metrics.MetricsReader;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 */
public final class AggregationTest extends Assert {
    private interface SubInterface{

    }
    private static final class TestAggregator extends AbstractAggregator implements SubInterface {

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
    public void serviceRetrievingTest() {
        final TestAggregator provider = new TestAggregator();
        assertNotNull(provider.queryObject(StringBuilder.class));
        assertNotNull(provider.queryObject(Short[].class));
        assertNotNull(provider.queryObject(SubInterface.class));
        assertNull(provider.queryObject(MetricsReader.class));
    }

    @Test
    public void inlineAggregationTest(){
        final Aggregator aggregator = AbstractAggregator.builder()
                .<CharSequence>aggregate(CharSequence.class, () -> "Frank Underwood")
                .aggregate(int[].class, new int[]{42, 43})
                .build();
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
    }

    @Test
    public void composeTest(){
        final Aggregator aggregator1 = AbstractAggregator.builder()
                .<CharSequence>aggregate(CharSequence.class, () -> "Frank Underwood")
                .aggregate(int[].class, new int[]{42, 43})
                .build();
        final Aggregator aggregator2 = AbstractAggregator.builder()
                .aggregate(Long.class, 56L)
                .<Boolean>aggregate(Boolean.class, () -> true)
                .build();
        final Aggregator aggregator = Aggregator.compose(aggregator1, aggregator2);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class));
    }
}
