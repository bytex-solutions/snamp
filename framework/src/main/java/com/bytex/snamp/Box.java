package com.bytex.snamp;

import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.*;

/**
 * Represents simple data container.
 * @param <T> Type of the data encapsulated inside of container.
 * @author Roman Sakno
 * @version 2.1
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

    default boolean ifPresent(final Consumer<? super T> consumer) {
        final T v = get();
        if (v == null)
            return false;
        else {
            consumer.accept(v);
            return true;
        }
    }

    <R> Optional<R> map(final Function<? super T, ? extends R> mapper);

    default OptionalInt mapToInt(final ToIntFunction<? super T> mapper) {
        return map(v -> OptionalInt.of(mapper.applyAsInt(v))).orElseGet(OptionalInt::empty);
    }

    default OptionalLong mapToLong(final ToLongFunction<? super T> mapper){
        return map(v -> OptionalLong.of(mapper.applyAsLong(v))).orElseGet(OptionalLong::empty);
    }

    default OptionalDouble mapToDouble(final ToDoubleFunction<? super T> mapper) {
        return map(v -> OptionalDouble.of(mapper.applyAsDouble(v))).orElseGet(OptionalDouble::empty);
    }

    /**
     * Determines whether this container has stored value.
     * @return {@literal true}, if this container has stored value.
     */
    boolean hasValue();

    static <T> Box<T> of(final T initialValue){
        return new MutableReference<>(initialValue);
    }
}
