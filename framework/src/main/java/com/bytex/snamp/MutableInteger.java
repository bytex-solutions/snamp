package com.bytex.snamp;

import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Represents mutable container for {@code int} data type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe(false)
public class MutableInteger extends Number implements IntSupplier, IntConsumer, Comparable<IntSupplier> {
    private static final long serialVersionUID = 6827831423137642850L;
    private int value;

    public MutableInteger(final int value){
        this.value = value;
    }

    public MutableInteger(){
        this(0);
    }

    public int getAndIncrement() {
        return value++;
    }

    public int incrementAndGet(){
        return ++value;
    }

    public <R> R get(final IntFunction<R> fn){
        return fn.apply(value);
    }

    public int set(final int right, final IntBinaryOperator operator) {
        return value = operator.applyAsInt(value, right);
    }

    /**
     * Returns the value of the specified number as an {@code int},
     * which may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code int}.
     */
    @Override
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of the specified number as a {@code long},
     * which may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code long}.
     */
    @Override
    public long longValue() {
        return value;
    }

    /**
     * Returns the value of the specified number as a {@code float},
     * which may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code float}.
     */
    @Override
    public float floatValue() {
        return value;
    }

    /**
     * Returns the value of the specified number as a {@code double},
     * which may involve rounding.
     *
     * @return the numeric value represented by this object after conversion
     * to type {@code double}.
     */
    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Change value stored in this container.
     * @param value A new value to store.
     */
    @Override
    public void accept(final int value) {
        set(value);
    }

    /**
     * Change value stored in this container.
     * @param value A new value to store.
     */
    public void set(final int value){
        this.value = value;
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public int getAsInt() {
        return value;
    }

    @Override
    public int compareTo(final IntSupplier other) {
        return Integer.compare(value, other.getAsInt());
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    private boolean equals(final Number other){
        return value == other.intValue();
    }

    private boolean equals(final IntSupplier other){
        return value == other.getAsInt();
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Number)
            return equals((Number) other);
        else
            return other instanceof IntSupplier && equals((IntSupplier) other);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
