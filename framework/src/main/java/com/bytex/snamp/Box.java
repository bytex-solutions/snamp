package com.bytex.snamp;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents simple data container.
 * @param <T> Type of the data encapsulated inside of container.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Box<T> extends Serializable, Supplier<T>, Consumer<T>, Acceptor<T, ExceptionPlaceholder>, Stateful {
    /**
     * Gets value stored in this container.
     * @return Value stored in this container.
     */
    @Override
    T get();

    /**
     * Sets value stored in this container.
     * @param value Value stored in this container.
     */
    void set(final T value);

    T setIfAbsent(final Supplier<? extends T> valueProvider);

    T accumulateAndGet(final T right, final BinaryOperator<T> operator);

    T updateAndGet(final UnaryOperator<T> operator);

    T getAndSet(final T newValue);

    T getOrDefault(final Supplier<? extends T> defaultProvider);

    /**
     * Determines whether this container has stored value.
     * @return {@literal true}, if this container has stored value.
     */
    boolean hasValue();

    static <T> Box<T> of(final T initialValue){
        return new MutableReference<>(initialValue);
    }
}
