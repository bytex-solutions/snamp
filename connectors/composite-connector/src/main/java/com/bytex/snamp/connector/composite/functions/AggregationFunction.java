package com.bytex.snamp.connector.composite.functions;

import com.google.common.collect.ImmutableList;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
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
    private final ImmutableList<String> operands;

    AggregationFunction(final OpenType<T> rt, final String... operands){
        this.returnType = Objects.requireNonNull(rt);
        this.operands = ImmutableList.copyOf(operands);
    }

    final <P extends Comparable<P>> P getOperand(final int index, final SimpleType<P> type, final OperandResolver resolver) throws Exception {
        final String name = operands.get(index);
        return resolver.resolveAs(name, type);
    }

    /**
     * Detects valid input type for this function.
     * @param inputType Input type to check.
     * @return {@literal true}, if this function can accept a value of the specified type; otherwise, {@literal false}.
     */
    public abstract boolean canAccept(final OpenType<?> inputType);

    /**
     * Gets return type of this function.
     * @return Return type of this function.
     */
    public final OpenType<T> getReturnType(){
        return returnType;
    }

    /**
     * Invokes aggregation function.
     * @param input Input value to process. Cannot be {@literal null}.
     * @param resolver A function used to resolve operands.
     * @return Function result.
     * @throws IllegalArgumentException Unsupported input value.
     * @throws IllegalStateException Unresolved operand.
     */
    public abstract T compute(final Object input, final OperandResolver resolver) throws Exception;

    final T compute(final Object input) throws Exception{
        return compute(input, null);
    }
}
