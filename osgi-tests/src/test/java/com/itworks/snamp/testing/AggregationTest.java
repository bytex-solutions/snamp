package com.itworks.snamp.testing;

import com.itworks.snamp.AbstractAggregator;
import org.junit.Test;

/**
 * @author Roman Sakno
 */
public final class AggregationTest extends AbstractUnitTest<AbstractAggregator> {
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
