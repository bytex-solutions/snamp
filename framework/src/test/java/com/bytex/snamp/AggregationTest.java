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
        private final Aggregator fallback;

        private TestAggregator() {
            fallback = builder()
                    .add(BigInteger.class, () -> BigInteger.ONE)
                    .add(BigDecimal.class, this::getDecimal)
                    .build();
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

        /**
         * Retrieves the aggregated object.
         *
         * @param objectType Type of the aggregated object.
         * @return An instance of the requested object; or {@literal null} if object is not available.
         */
        @Override
        public <T> T queryObject(final Class<T> objectType) {
            return queryObject(objectType, fallback);
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
        final Aggregator aggregator = AbstractAggregator.builder()
                .add(CharSequence.class, () -> "Frank Underwood")
                .add(int[].class, () -> new int[]{42, 43})
                .build();
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
    }

    @Test
    public void composeTest() {
        final Aggregator aggregator1 = AbstractAggregator.builder()
                .add(CharSequence.class, () -> "Frank Underwood")
                .add(int[].class, () -> new int[]{42, 43})
                .build();
        final Aggregator aggregator2 = AbstractAggregator.builder()
                .add(Long.class, () -> 56L)
                .add(Boolean.class, () -> true)
                .build();
        final Aggregator aggregator = aggregator1.compose(aggregator2);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class));
    }

    @Test
    public void composeTest2() {
        final Aggregator aggregator1 = AbstractAggregator.builder()
                .add(CharSequence.class, () -> "Frank Underwood")
                .add(int[].class, () -> new int[]{42, 43})
                .build();
        final Aggregator aggregator2 = AbstractAggregator.builder()
                .add(Long.class, () -> 56L)
                .add(Boolean.class, () -> true)
                .build();
        final Aggregator aggregator3 = AbstractAggregator.builder()
                .add(BigInteger.class, () -> BigInteger.TEN)
                .build();
        final Aggregator aggregator4 = AbstractAggregator.builder()
                .add(BigDecimal.class, () -> BigDecimal.ONE)
                .build();
        final Aggregator aggregator = aggregator1.compose(aggregator2).compose(aggregator3).compose(aggregator4);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class));
        assertEquals(BigInteger.TEN, aggregator.queryObject(BigInteger.class));
        //assertEquals(BigDecimal.ONE, aggregator.queryObject(BigDecimal.class));
    }
}
