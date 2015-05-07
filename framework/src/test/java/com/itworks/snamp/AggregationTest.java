package com.itworks.snamp;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 */
public final class AggregationTest extends Assert {
    private static final class TestAggregator extends AbstractAggregator {

        @Aggregation
        private final StringBuilder service1 = new StringBuilder("Hello, world!");

        @Aggregation
        public Short[] getService2(){
            return new Short[]{1, 2, 3};
        }
    }


    @Test
    public final void serviceRetrievingTest(){
        final TestAggregator provider = new TestAggregator();
        assertTrue(provider.queryObject(StringBuilder.class) != null);
        assertTrue(provider.queryObject(Short[].class) != null);
    }
}
