package com.bytex.snamp;

import java.util.function.BooleanSupplier;

/**
 * Represents unary operator with {@code boolean} operands.
 * @since 2.0
 * @version 2.0
 */
@FunctionalInterface
public interface BooleanUnaryOperator {
    boolean applyAsBoolean(final boolean value);

    default BooleanSupplier capture(final BooleanSupplier value){
        return () -> applyAsBoolean(value.getAsBoolean());
    }

    default BooleanUnaryOperator andThen(final BooleanUnaryOperator operator){
        return value -> operator.applyAsBoolean(applyAsBoolean(value));
    }

    default BooleanUnaryOperator compose(final BooleanUnaryOperator operator){
        return value -> applyAsBoolean(operator.applyAsBoolean(value));
    }
}
