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
    @Test
    public void parseMaxFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("max()");
        fn.compute(10);
        fn.compute(20);
        assertEquals(20.0, fn.compute(5, null));
    }

    @Test
    public void parseMinFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("min()");
        fn.compute(10);
        fn.compute(5);
        assertEquals(5.0, fn.compute(20));
    }

    @Test
    public void parseSumFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("sum(10s)");
        fn.compute(10);
        fn.compute(5);
        assertEquals(30.0, fn.compute(15));
    }

    @Test
    public void parseAvgFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("avg(1s)");
        fn.compute(10);
        fn.compute(20);
        fn.compute(30);
        assertEquals(16.25, fn.compute(5));
        Thread.sleep(1001);
        assertEquals(5.0, fn.compute(5));
    }

    @Test
    public void parsePercentileFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("percentile(90, 1s)");
        fn.compute(10);
        fn.compute(20);
        fn.compute(30);
        fn.compute(45);
        fn.compute(22);
        fn.compute(64);
        fn.compute(53);
        final Percentile p = new Percentile(90);
        final double expected = p.evaluate(new double[]{10, 20, 30, 45, 22, 64, 53, 49});
        assertEquals(expected, fn.compute(49));
    }

    @Test
    public void parseCorrelationFunction() throws Exception {
        final AggregationFunction<?> fn = FunctionParser.parse("correl($other)");
        final OperandResolver resolver = new OperandResolver() {
            private double value = 0;

            @SuppressWarnings("unchecked")
            @Override
            public <T> T resolveAs(final String operand, final SimpleType<T> expectedType) throws Exception {
                assertEquals("other", operand);
                return (T)WellKnownType.getType(expectedType).cast(value++);
            }
        };
        fn.compute(0, resolver);
        fn.compute(1, resolver);
        fn.compute(2, resolver);
        fn.compute(3, resolver);
        fn.compute(4, resolver);
        assertEquals(1.0, (Double) fn.compute(5, resolver), 0.01);
    }
}
