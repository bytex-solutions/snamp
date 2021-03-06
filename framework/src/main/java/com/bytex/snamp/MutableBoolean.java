package com.bytex.snamp;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.*;

/**
 * Represents mutable container for {@code boolean} data type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@NotThreadSafe
final class MutableBoolean implements BooleanBox {
    private static final long serialVersionUID = 5106640286011881150L;
    private boolean value;

    MutableBoolean(final boolean value){
        this.value = value;
    }

    @Override
    public Boolean setIfAbsent(final Supplier<? extends Boolean> valueProvider) {
        return value;
    }

    @Override
    public void reset() {
        value = false;
    }

    @Override
    public boolean ifPresent(final Consumer<? super Boolean> consumer) {
        consumer.accept(value);
        return true;
    }

    @Override
    public <R> Optional<R> map(final Function<? super Boolean, ? extends R> mapper) {
        return Optional.of(value).map(mapper);
    }

    @Override
    public OptionalInt mapToInt(final ToIntFunction<? super Boolean> mapper) {
        return OptionalInt.of(mapper.applyAsInt(value));
    }

    @Override
    public OptionalLong mapToLong(final ToLongFunction<? super Boolean> mapper) {
        return OptionalLong.of(mapper.applyAsLong(value));
    }

    @Override
    public OptionalDouble mapToDouble(final ToDoubleFunction<? super Boolean> mapper) {
        return OptionalDouble.of(mapper.applyAsDouble(value));
    }

    /**
     * Gets value stored in this container.
     *
     * @return Value stored in this container.
     */
    @Override
    public Boolean get() {
        return getAsBoolean();
    }

    /**
     * Gets a boolean value stored in this container.
     * @return A boolean value stored in this container.
     */
    @Override
    public boolean getAsBoolean(){
        return value;
    }

    @Override
    public void set(final boolean newValue) {
        value = newValue;
    }

    @Override
    public boolean getAndSet(final boolean newValue) {
        final boolean prev = value;
        set(newValue);
        return prev;
    }

    @Override
    public boolean accumulateAndGet(final boolean right, final BooleanBinaryOperator operator) {
        return value = operator.applyAsBoolean(value, right);
    }

    @Override
    public boolean updateAndGet(final BooleanUnaryOperator operator) {
        return value = operator.applyAsBoolean(value);
    }

    /**
     * Sets value stored in this container.
     *
     * @param value Value stored in this container.
     */
    @Override
    public void set(final Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean accumulateAndGet(final Boolean right, final BinaryOperator<Boolean> operator) {
        return value = operator.apply(value, right);
    }

    @Override
    public Boolean updateAndGet(final UnaryOperator<Boolean> operator) {
        return value = operator.apply(value);
    }

    @Override
    public Boolean getAndSet(final Boolean newValue) {
        final boolean prev = value;
        set(newValue);
        return prev;
    }

    @Override
    public Boolean getOrDefault(final Supplier<? extends Boolean> defaultProvider) {
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

    @Override
    public void accept(final Boolean newValue) {
        set(newValue);
    }

    @Override
    public int compareTo(final boolean other){
        return Boolean.compare(value, other);
    }

    @Override
    public int compareTo(@Nonnull final BooleanSupplier other) {
        return compareTo(other.getAsBoolean());
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    private boolean equals(final boolean other){
        return value == other;
    }

    private boolean equals(final BooleanSupplier other){
        return equals(other.getAsBoolean());
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Boolean)
            return equals((boolean) other);
        else
            return other instanceof BooleanSupplier && equals((BooleanSupplier) other);
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
