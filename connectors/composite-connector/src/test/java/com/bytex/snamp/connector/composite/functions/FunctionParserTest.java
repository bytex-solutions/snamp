package com.bytex.snamp.connector.composite.functions;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.junit.Assert;
import org.junit.Test;

/**
 * Represents test for {@link FunctionParser}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FunctionParserTest extends Assert {
    @Test
    public void parseMaxFunction() throws FunctionParserException {
        final AggregationFunction<?> fn = FunctionParser.parse("max()", ReferenceResolver.EMPTY);
        fn.compute(10);
        fn.compute(20);
        assertEquals(20.0, fn.compute(5));
    }

    @Test
    public void parseMinFunction() throws FunctionParserException {
        final AggregationFunction<?> fn = FunctionParser.parse("min()", ReferenceResolver.EMPTY);
        fn.compute(10);
        fn.compute(5);
        assertEquals(5.0, fn.compute(20));
    }

    @Test
    public void parseSumFunction() throws FunctionParserException {
        final AggregationFunction<?> fn = FunctionParser.parse("sum(10s)", ReferenceResolver.EMPTY);
        fn.compute(10);
        fn.compute(5);
        assertEquals(30.0, fn.compute(15));
    }

    @Test
    public void parseAvgFunction() throws FunctionParserException, InterruptedException {
        final AggregationFunction<?> fn = FunctionParser.parse("avg(1s)", ReferenceResolver.EMPTY);
        fn.compute(10);
        fn.compute(20);
        fn.compute(30);
        assertEquals(16.25, fn.compute(5));
        Thread.sleep(1001);
        assertEquals(5.0, fn.compute(5));
    }

    @Test
    public void parsePercentileFunction() throws FunctionParserException{
        final AggregationFunction<?> fn = FunctionParser.parse("percentile(90, 1s)", ReferenceResolver.EMPTY);
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
}
