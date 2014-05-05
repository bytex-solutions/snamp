package com.itworks.snamp.testing;

import com.itworks.snamp.AbstractAggregator;
import org.junit.Test;
import org.apache.commons.lang3.Range;

/**
 * @author Roman Sakno
 */
public final class AggregationTest extends AbstractUnitTest<AbstractAggregator> {
    private static final class TestAggregator extends AbstractAggregator {

        @Aggregation
        private final Range<Integer> service1 = Range.between(0, 10);

        @Aggregation
        public Short[] getService2(){
            return new Short[]{1, 2, 3};
        }
    }

    public AggregationTest(){
        super(AbstractAggregator.class);
    }

    @Test
    public final void serviceRetrievingTest(){
        final TestAggregator provider = new TestAggregator();
        assertTrue(provider.queryObject(Range.class) instanceof Range);
        assertTrue(provider.queryObject(Short[].class) instanceof Short[]);
    }
}
