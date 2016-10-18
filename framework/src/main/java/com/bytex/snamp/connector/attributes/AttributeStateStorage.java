package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.Box;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AttributeStateStorage<T extends Serializable> implements Box<T> {
    private static final long serialVersionUID = 5742599423711980037L;
    private final ConcurrentMap<String, Object> storage;
    private final String storageKey;
    private final Class<T> storageType;

    AttributeStateStorage(final ConcurrentMap<String, Object> storage,
                          final String storageKey,
                          final Class<T> storageType){
        this.storage = Objects.requireNonNull(storage);
        this.storageKey = Objects.requireNonNull(storageKey);
        this.storageType = Objects.requireNonNull(storageType);
    }


    /**
     * Gets value stored in this container.
     *
     * @return Value stored in this container.
     */
    @Override
    public T get() {
        return storageType.cast(storage.get(storageKey));
    }

    /**
     * Sets value stored in this container.
     *
     * @param value Value stored in this container.
     */
    @Override
    public void set(final T value) {
        storage.put(storageKey, value);
    }

    @Override
    public T setIfAbsent(final Supplier<? extends T> valueProvider) {
        if(hasValue())
            return get();
        final T newValue = valueProvider.get();
        final Object previous = storage.putIfAbsent(storageKey, newValue);
        return storageType.isInstance(previous) ? storageType.cast(previous) : newValue;
    }

    @Override
    public T accumulateAndGet(final T right, final BinaryOperator<T> operator) {
        final T result;
        storage.put(storageKey, result = operator.apply(get(), right));
        return result;
    }

    @Override
    public T accumulateAndGet(final UnaryOperator<T> operator) {
        final T result;
        storage.put(storageKey, result = operator.apply(get()));
        return result;
    }

    @Override
    public T getAndSet(final T newValue) {
        return storageType.cast(storage.put(storageKey, newValue));
    }

    @Override
    public T getOrDefault(final Supplier<? extends T> defaultProvider) {
        final Object result = storage.getOrDefault(storageKey, defaultProvider);
        return Objects.equals(defaultProvider, result) ? defaultProvider.get() : storageType.cast(result);
    }

    /**
     * Determines whether this container has stored value.
     *
     * @return {@literal true}, if this container has stored value.
     */
    @Override
    public boolean hasValue() {
        return storage.containsKey(storageKey);
    }

    @Override
    public void accept(final T value) {
        set(value);
    }
}
