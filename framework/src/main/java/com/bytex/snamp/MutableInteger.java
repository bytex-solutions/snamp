package com.bytex.snamp;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.*;

/**
 * Represents mutable container for {@code int} data type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@NotThreadSafe
final class MutableInteger extends Number implements IntBox {
    private static final long serialVersionUID = 6827831423137642850L;
    private int value;

    MutableInteger(final int value){
        this.value = value;
    }

    @Override
    public void reset() {
        value = 0;
    }

    @Override
    public Integer setIfAbsent(final Supplier<? extends Integer> valueProvider) {
        return value;
    }

    @Override
    public int getAndIncrement() {
        return value++;
    }

    @Override
    public int incrementAndGet(){
        return ++value;
    }

    @Override
    public long mapToLong(final IntToLongFunction mapper) {
        return mapper.applyAsLong(value);
    }

    @Override
    public double mapToDouble(final IntToDoubleFunction mapper) {
        return mapper.applyAsDouble(value);
    }

    @Override
    public boolean ifPresent(final Consumer<? super Integer> consumer) {
        consumer.accept(value);
        return true;
    }

    @Override
    public <R> Optional<R> map(final Function<? super Integer, ? extends R> mapper) {
        return Optional.of(value).map(mapper);
    }

    @Override
    public OptionalInt mapToInt(final ToIntFunction<? super Integer> mapper) {
        return OptionalInt.of(mapper.applyAsInt(value));
    }

    @Override
    public OptionalLong mapToLong(final ToLongFunction<? super Integer> mapper) {
        return OptionalLong.of(mapper.applyAsLong(value));
    }

    @Override
    public OptionalDouble mapToDouble(final ToDoubleFunction<? super Integer> mapper) {
        return OptionalDouble.of(mapper.applyAsDouble(value));
    }

    @Override
    public int accumulateAndGet(final int right, final IntBinaryOperator operator) {
        return value = operator.applyAsInt(value, right);
    }

    @Override
    public int getAndSet(final int newValue) {
        final int prev = value;
        value = newValue;
        return prev;
    }

    @Override
    public int updateAndGet(final IntUnaryOperator operator) {
        return value = operator.applyAsInt(value);
    }

    /**
     * Gets value stored in this container.
     *
     * @return Value stored in this container.
     */
    @Override
    public Integer get() {
        return getAsInt();
    }

    @Override
    public void set(final Integer newValue) {
        value = newValue;
    }

    @Override
    public Integer accumulateAndGet(final Integer right, final BinaryOperator<Integer> operator) {
        return value = operator.apply(value, right);
    }

    @Override
    public Integer updateAndGet(final UnaryOperator<Integer> operator) {
        return value = operator.apply(value);
    }

    @Override
    public Integer getAndSet(final Integer newValue) {
        final int prev = value;
        value = newValue;
        return prev;
    }

    @Override
    public Integer getOrDefault(final Supplier<? extends Integer> defaultProvider) {
        return value;
    }

    /**
     * Determines whether this container has stored value.
     *
     * @return {@literal true}, if this container has stored value.
     */
    @Override
    public boolean hasValue() {
        return true;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    @Override
    public void accept(final Integer value) {
        set(value);
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

    public int compareTo(final int other){
        return Integer.compare(value, other);
    }

    @Override
    public int compareTo(@Nonnull final IntSupplier other) {
        return compareTo(other.getAsInt());
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
