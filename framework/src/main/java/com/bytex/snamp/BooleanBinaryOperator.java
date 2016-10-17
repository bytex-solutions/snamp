package com.bytex.snamp;

/**
 * Represents binary operator for {@code boolean} data type.
 * @since 2.0
 * @version 2.0
 */
@FunctionalInterface
public interface BooleanBinaryOperator {
    boolean applyAsBoolean(final boolean left, final boolean right);
}
