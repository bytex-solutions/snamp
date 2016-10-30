package com.bytex.snamp;

import java.util.function.BooleanSupplier;

/**
 * Represents mutable container for {@code boolean}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface BooleanBox extends Box<Boolean>, BooleanSupplier, Comparable<BooleanSupplier> {

    void set(final boolean value);

    boolean getAndSet(final boolean value);

    boolean accumulateAndGet(final boolean right, final BooleanBinaryOperator operator);

    boolean accumulateAndGet(final BooleanUnaryOperator operator);

    int compareTo(final boolean value);
}