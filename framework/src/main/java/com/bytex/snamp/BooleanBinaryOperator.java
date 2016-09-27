package com.bytex.snamp;

import java.util.function.BooleanSupplier;

/**
 * Represents binary operator for {@code boolean} data type.
 * @since 2.0
 * @version 2.0
 */
@FunctionalInterface
public interface BooleanBinaryOperator {
    boolean applyAsBoolean(final boolean left, final boolean right);

    default BooleanSupplier capture(final BooleanSupplier left, final BooleanSupplier right) {
        return () -> applyAsBoolean(left.getAsBoolean(), right.getAsBoolean());
    }

    default BooleanUnaryOperator captureLeft(final BooleanSupplier left){
        return right -> applyAsBoolean(left.getAsBoolean(), right);
    }

    default BooleanUnaryOperator captureRight(final BooleanSupplier right){
        return left -> applyAsBoolean(left, right.getAsBoolean());
    }
}
