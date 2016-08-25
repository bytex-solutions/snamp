package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * Represents aggregation function.
 * @param <T> Return type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AggregationFunction<T> implements Expression {
    private final OpenType<T> returnType;

    AggregationFunction(final OpenType<T> rt){
        this.returnType = Objects.requireNonNull(rt);
    }

    final OpenType<T> getReturnType(){
        return returnType;
    }

    abstract T compute(final Object input);
}
