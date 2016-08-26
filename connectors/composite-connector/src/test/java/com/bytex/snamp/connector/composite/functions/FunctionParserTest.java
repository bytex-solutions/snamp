package com.bytex.snamp.connector.composite.functions;

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
        final AggregationFunction<?> fn = FunctionParser.parse("max()");
        fn.compute(10);
        fn.compute(20);
        assertEquals(20.0, fn.compute(5));
    }

    @Test
    public void parseMinFunction() throws FunctionParserException {
        final AggregationFunction<?> fn = FunctionParser.parse("min()");
        fn.compute(10);
        fn.compute(5);
        assertEquals(5.0, fn.compute(20));
    }

    @Test
    public void parseSumFunction() throws FunctionParserException {
        final AggregationFunction<?> fn = FunctionParser.parse("sum()");
        fn.compute(10);
        fn.compute(5);
        assertEquals(30.0, fn.compute(15));
    }

    @Test
    public void parseAvgFunction() throws FunctionParserException, InterruptedException {
        final AggregationFunction<?> fn = FunctionParser.parse("avg(1s)");
        fn.compute(10);
        fn.compute(20);
        assertEquals(15.0, fn.compute(15));
        Thread.sleep(1001);
        assertEquals(5.0, fn.compute(5));
    }

    @Test
    public void parsePercentileFunction() throws FunctionParserException{
        final AggregationFunction<?> fn = FunctionParser.parse("percentile(90, 1s)");
        fn.compute(10);
        fn.compute(20);
        fn.compute(30);
        fn.compute(45);
        fn.compute(22);
        fn.compute(64);
        fn.compute(53);
        assertEquals(56.3, fn.compute(49));
    }
}
