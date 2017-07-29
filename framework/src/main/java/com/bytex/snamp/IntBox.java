package com.bytex.snamp;

import java.util.function.*;

/**
 * Represents mutable container for {@code int} data type.
 * @author Roman Sakno
 * @version 2.1
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
    long mapToLong(final IntToLongFunction mapper);
    double mapToDouble(final IntToDoubleFunction mapper);

    static IntBox of(final int initialValue){
        return new MutableInteger(initialValue);
    }
}
