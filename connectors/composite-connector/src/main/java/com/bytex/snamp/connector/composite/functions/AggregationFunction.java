package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
 * Represents aggregation function.
 * @param <T> Return type.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class AggregationFunction<T> implements Expression {
    private final OpenType<T> returnType;

    AggregationFunction(final OpenType<T> rt){
        this.returnType = Objects.requireNonNull(rt);
    }

    /**
     * Detects valid input type for this function.
     * @param index Parameter position.
     * @param inputType Input type to check.
     * @return {@literal true}, if this function can accept a value of the specified type; otherwise, {@literal false}.
     */
    public abstract boolean canAccept(final int index, final OpenType<?> inputType);

    /**
     * Gets return type of this function.
     * @return Return type of this function.
     */
    public final OpenType<T> getReturnType(){
        return returnType;
    }

    /**
     * Invokes aggregation function.
     * @param context A function used to resolve operands.
     * @param args Arguments of the function.
     * @return Function result.
     * @throws IllegalArgumentException Unsupported input value.
     * @throws IllegalStateException Unresolved operand.
     */
    public abstract T eval(final EvaluationContext context, final Object... args) throws Exception;
}
