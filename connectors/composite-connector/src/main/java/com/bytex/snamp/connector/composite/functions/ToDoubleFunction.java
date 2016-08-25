package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.SimpleType;

/**
 * Represents abstract class for all functions returning {@code double} result.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ToDoubleFunction extends AggregationFunction<Double> {
    ToDoubleFunction(){
        super(SimpleType.DOUBLE);
    }

    abstract double compute(final double input);

    @Override
    final Double compute(final Object input) {
        return input instanceof Number ? compute(((Number)input).doubleValue()) : 0F;
    }
}
