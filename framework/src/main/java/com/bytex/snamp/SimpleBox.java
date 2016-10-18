package com.bytex.snamp;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents simple container that holds mutable typed value.
 * @param <T> Type of the value in the container.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SimpleBox<T> implements Box<T>, Supplier<T>, Consumer<T>, Acceptor<T, ExceptionPlaceholder>, Serializable{
    private static final long serialVersionUID = -3932725773035687013L;
    private T value;

    /**
     * Initializes a new instance of the mutable container.
     * @param initial The initial value of placed to the container.
     */
    SimpleBox(final T initial){
        this.value = initial;
    }

    @Override
    public T setIfAbsent(final Supplier<? extends T> valueProvider) {
        if(value == null)
            value = valueProvider.get();
        return value;
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value The value to process.
     */
    @Override
    public void accept(final T value) {
        set(value);
    }

    /**
     * Retrieves an instance of the stored object.
     *
     * @return An object stored in this box.
     */
    @Override
    public T get() {
        return value;
    }

    /**
     * Gets the value stored in this box and overwrites this value after.
     * @param newValue A new value to be stored in this box.
     * @return An existing value in this box.
     */
    @Override
    public T getAndSet(final T newValue){
        final T result = value;
        value = newValue;
        return result;
    }

    @Override
    public T accumulateAndGet(final T right, final BinaryOperator<T> operator) {
        return value = operator.apply(value, right);
    }

    @Override
    public T accumulateAndGet(final UnaryOperator<T> operator) {
        return value = operator.apply(value);
    }

    /**
     * Determines whether object in this container is not {@literal null}.
     * @return {@literal true}, if object in this container is not {@literal null}; otherwise, {@literal false}.
     * @since 1.2
     */
    @Override
    public boolean hasValue(){
        return value != null;
    }

    /**
     * Retrieves an instance of the stored object or returns alternative value
     * if stored object is {@literal null}.
     * @param defval The alternative value to return.
     * @return An object stored in this box; or {@code defval} if stored object is {@literal null}.
     */
    public T getOrDefault(final Supplier<? extends T> defval){
        return value == null ? defval.get() : value;
    }

    /**
     * Places a new value into this container.
     * @param value A new value to store into this container.
     */
    @Override
    public void set(final T value){
        this.value = value;
    }

    /**
     * Returns string representation of the aggregated object.
     * @return The string representation of the aggregated object.
     */
    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
