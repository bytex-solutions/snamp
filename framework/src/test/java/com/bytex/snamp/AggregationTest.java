package com.bytex.snamp;

import com.bytex.snamp.connectors.metrics.MetricsReader;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Roman Sakno
 */
public final class AggregationTest extends Assert {
    private interface SubInterface{

    }

    private static final class TestAggregator extends AbstractAggregator implements SubInterface {
        private TestAggregator() {
        }

        @Override
        protected void registerExtraAggregations(final AggregationBuilder customAggregations) {
            customAggregations
                    .add(BigInteger.class, () -> BigInteger.ONE)
                    .add(BigDecimal.class, this::getDecimal);
        }

        private BigDecimal getDecimal(){
            return BigDecimal.TEN;
        }

        @Aggregation
        @SpecialUse
        private StringBuilder service1 = new StringBuilder("Hello, world!");

        @Aggregation
        @SpecialUse
        public short[] getService2(){
            return new short[]{1, 2, 3};
        }
    }


    @Test
    public void serviceRetrievingTest() {
        final TestAggregator provider = new TestAggregator();
        assertNotNull(provider.queryObject(StringBuilder.class));
        assertNotNull(provider.queryObject(short[].class));
        assertNotNull(provider.queryObject(SubInterface.class));
        assertNotNull(provider.queryObject(BigInteger.class));
        assertNotNull(provider.queryObject(BigDecimal.class));
        assertNull(provider.queryObject(MetricsReader.class));
    }

    @Test
    public void inlineAggregationTest(){
        final Aggregator aggregator = AbstractAggregator.of( CharSequence.class, () -> "Frank Underwood", int[].class, () -> new int[]{42, 43});
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
    }

    @Test
    public void composeTest(){
        final Aggregator aggregator1 = AbstractAggregator.of(CharSequence.class, () -> "Frank Underwood", int[].class, () -> new int[]{42, 43});
        final Aggregator aggregator2 = AbstractAggregator.of(Long.class, () -> 56L, Boolean.class, () -> true);
        final Aggregator aggregator = Aggregator.compose(aggregator1, aggregator2);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class));
    }
}
