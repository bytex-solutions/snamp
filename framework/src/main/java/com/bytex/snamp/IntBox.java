package com.bytex.snamp;

import java.util.function.*;

/**
 * Represents mutable container for {@code int} data type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface IntBox extends Box<Integer>, IntSupplier, IntConsumer, Comparable<IntSupplier> {
    void set(final int newValue);
    int getAndSet(final int newValue);
    int getAndIncrement();
    int incrementAndGet();
    int accumulateAndGet(final int right, final IntBinaryOperator operator);
    int updateAndGet(final IntUnaryOperator operator);
    int compareTo(final int newValue);
    <R> R get(final IntFunction<? extends R> fn);
}
