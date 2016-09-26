package com.bytex.snamp;

/**
 * Represents binary operator for {@code boolean} data type.
 */
@FunctionalInterface
public interface BooleanBinaryOperator {
    boolean applyAsBoolean(final boolean current, final boolean provided);
}
