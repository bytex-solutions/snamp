package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;

import static com.bytex.snamp.ArrayUtils.emptyArray;

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
                    .addValue(BigInteger.class, BigInteger.ONE)
                    .add(BigDecimal.class, this::getDecimal)
                    .build();
        }

        private BigDecimal getDecimal(){
            return BigDecimal.TEN;
        }

        @Aggregation
        @SpecialUse(SpecialUse.Case.REFLECTION)
        private StringBuilder service1 = new StringBuilder("Hello, world!");

        @Aggregation
        @SpecialUse(SpecialUse.Case.REFLECTION)
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
        public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
            return queryObject(objectType, fallback);
        }
    }


    @Test
    public void serviceRetrievingTest() {
        final TestAggregator provider = new TestAggregator();
        assertTrue(provider.queryObject(StringBuilder.class).isPresent());
        assertTrue(provider.queryObject(short[].class).isPresent());
        assertTrue(provider.queryObject(SubInterface.class).isPresent());
        assertTrue(provider.queryObject(BigInteger.class).isPresent());
        assertTrue(provider.queryObject(BigDecimal.class).isPresent());
    }

    @Test
    public void inlineAggregationTest(){
        final Aggregator aggregator = AbstractAggregator.builder()
                .add(CharSequence.class, () -> "Frank Underwood")
                .add(int[].class, () -> new int[]{42, 43})
                .build();
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class).orElse(""));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class).orElse(emptyArray(int[].class)));
    }

    @Test
    public void composeTest() {
        final Aggregator aggregator1 = AbstractAggregator.builder()
                .addSupplier(CharSequence.class, () -> "Frank Underwood")
                .addValue(int[].class, new int[]{42, 43})
                .build();
        final Aggregator aggregator2 = AbstractAggregator.builder()
                .add(Long.class, () -> 56L)
                .add(Boolean.class, () -> true)
                .build();
        final Aggregator aggregator = aggregator1.compose(aggregator2);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class).orElse(""));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class).orElse(emptyArray(int[].class)));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class).orElse(0L));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class).orElse(false));
    }

    @Test
    public void composeTest2() {
        final Aggregator aggregator1 = AbstractAggregator.builder()
                .add(CharSequence.class, () -> "Frank Underwood")
                .add(int[].class, () -> new int[]{42, 43})
                .build();
        final Aggregator aggregator2 = AbstractAggregator.builder()
                .addSupplier(Long.class, () -> 56L)
                .addValue(Boolean.class, true)
                .build();
        final Aggregator aggregator3 = AbstractAggregator.builder()
                .add(BigInteger.class, () -> BigInteger.TEN)
                .build();
        final Aggregator aggregator4 = AbstractAggregator.builder()
                .add(BigDecimal.class, () -> BigDecimal.ONE)
                .build();
        final Aggregator aggregator = aggregator1.compose(aggregator2).compose(aggregator3).compose(aggregator4);
        assertEquals("Frank Underwood", aggregator.queryObject(CharSequence.class).orElse(""));
        assertArrayEquals(new int[]{42, 43}, aggregator.queryObject(int[].class).orElse(emptyArray(int[].class)));
        assertEquals(new Long(56L), aggregator.queryObject(Long.class).orElse(0L));
        assertEquals(Boolean.TRUE, aggregator.queryObject(Boolean.class).orElse(false));
        assertEquals(BigInteger.TEN, aggregator.queryObject(BigInteger.class).orElse(BigInteger.ZERO));
        //assertEquals(BigDecimal.ONE, aggregator.queryObject(BigDecimal.class));
    }

    @Test
    public void splitTest(){
        final Integer[] array = {10, 30, 50, 90, 110, 0, 9, 8, 2, 10, 45, 32, 86};
        final List<Integer> list = Arrays.asList(array);
        final Spliterator<Integer> split = list.spliterator();
        final Spliterator<Integer> split1 = split.trySplit();
        System.out.println("Split1");
        split1.forEachRemaining(System.out::println);
        final Spliterator<Integer> split2 = split.trySplit();
        System.out.println("Split2");
        split2.forEachRemaining(System.out::println);
        final Spliterator<Integer> split3 = split.trySplit();
        System.out.println("Split3");
        split3.forEachRemaining(System.out::println);
    }
}
