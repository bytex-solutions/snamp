package com.snamp;

import org.junit.Test;

/**
 * @author roman
 */
public final class AggregationTest extends SnampClassTestSet<AbstractAggregated> {
    private static final class TestAggregator extends AbstractAggregated {

        @Aggregation
        private final Range<Integer> service1 = new Range<>(0, 10);

        @Aggregation
        public Short[] getService2(){
            return new Short[]{1, 2, 3};
        }
    }

    @Test
    public final void serviceRetrievingTest(){
        final TestAggregator provider = new TestAggregator();
        assertTrue(provider.queryObject(Range.class) instanceof Range);
        assertTrue(provider.queryObject(Short[].class) instanceof Short[]);
    }
}
