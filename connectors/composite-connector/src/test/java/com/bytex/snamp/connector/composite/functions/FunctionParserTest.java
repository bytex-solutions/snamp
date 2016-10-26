package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.jmx.WellKnownType;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.SimpleType;

/**
 * Represents test for {@link FunctionParser}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FunctionParserTest extends Assert {
    private static final NameResolver EMPTY_RESOLVER = new NameResolver() {
        @Override
        public <T> T resolveAs(final String name, final SimpleType<T> expectedType) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    };

    @Test
    public void parseMaxFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("max()");
        fn.invoke(EMPTY_RESOLVER, 10);
        fn.invoke(EMPTY_RESOLVER, 20);
        assertEquals(20.0, fn.invoke(EMPTY_RESOLVER, 5));
    }

    @Test
    public void parseMinFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("min()");
        fn.invoke(EMPTY_RESOLVER, 10);
        fn.invoke(EMPTY_RESOLVER, 5);
        assertEquals(5.0, fn.invoke(EMPTY_RESOLVER, 20));
    }

    @Test
    public void parseSumFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("sum(10s)");
        fn.invoke(EMPTY_RESOLVER, 10);
        fn.invoke(EMPTY_RESOLVER, 5);
        assertEquals(30.0, fn.invoke(EMPTY_RESOLVER, 15));
    }

    @Test
    public void parseAvgFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("avg(1s)");
        fn.invoke(EMPTY_RESOLVER, 10);
        fn.invoke(EMPTY_RESOLVER, 20);
        fn.invoke(EMPTY_RESOLVER, 30);
        assertEquals(16.25, fn.invoke(EMPTY_RESOLVER, 5));
        Thread.sleep(1001);
        assertEquals(5.0, fn.invoke(EMPTY_RESOLVER, 5));
    }

    @Test
    public void parsePercentileFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("percentile(90)");
        fn.invoke(EMPTY_RESOLVER, 10);
        fn.invoke(EMPTY_RESOLVER, 20);
        fn.invoke(EMPTY_RESOLVER, 30);
        fn.invoke(EMPTY_RESOLVER, 45);
        fn.invoke(EMPTY_RESOLVER, 22);
        fn.invoke(EMPTY_RESOLVER, 64);
        fn.invoke(EMPTY_RESOLVER, 53);
        final Percentile p = new Percentile(90);
        final double expected = p.evaluate(new double[]{10, 20, 30, 45, 22, 64, 53, 49});
        assertEquals(expected, fn.invoke(EMPTY_RESOLVER, 49));
    }

    @Test
    public void parseCorrelationFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("correl($other)");
        final NameResolver resolver = new NameResolver() {
            private double value = 0;

            @SuppressWarnings("unchecked")
            @Override
            public <T> T resolveAs(final String name, final SimpleType<T> expectedType) throws Exception {
                assertEquals("other", name);
                return (T)WellKnownType.getType(expectedType).cast(value++);
            }
        };
        fn.invoke(resolver, 0);
        fn.invoke(resolver, 1);
        fn.invoke(resolver, 2D);
        fn.invoke(resolver, 3D);
        fn.invoke(resolver, 4L);
        assertEquals(1D, (Double) fn.invoke(resolver, 5D), 0.01D);
    }
}
