package com.bytex.snamp;

/**
 * Represents unary operator with {@code boolean} operands.
 * @since 2.0
 * @version 2.0
 */
@FunctionalInterface
public interface BooleanUnaryOperator {
    BooleanUnaryOperator NEGATE = value -> !value;
    boolean applyAsBoolean(final boolean value);

    default BooleanUnaryOperator andThen(final BooleanUnaryOperator operator){
        return value -> operator.applyAsBoolean(applyAsBoolean(value));
    }

    default BooleanUnaryOperator compose(final BooleanUnaryOperator operator){
        return value -> applyAsBoolean(operator.applyAsBoolean(value));
    }
}
