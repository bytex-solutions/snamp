package com.bytex.snamp.concurrent;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.ExceptionalCallable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SimpleLazyValue<V> implements LazyValue<V> {
    private V value;
    private final Supplier<V> valueSupplier;

    SimpleLazyValue(final Supplier<V> activator){
        this.valueSupplier = Objects.requireNonNull(activator);
    }

    @Override
    public <G extends Throwable> void reset(final Consumer<? super V, G> cleaner) throws G {
        if (value != null)
            try {
                cleaner.accept(value);
            } finally {
                value = null;
            }
    }

    @Override
    public boolean isActivated() {
        return value != null;
    }

    @Override
    public V get() {
        return value == null ? (value = valueSupplier.get()) : value;
    }

    @Override
    public <E extends Exception> V get(final ExceptionalCallable<? extends V, E> activator) throws E {
        return isActivated() ? value : (value = activator.call());
    }

    @Override
    public V getIfActivated() throws IllegalStateException {
        if(isActivated()) return value;
        else throw new IllegalStateException();
    }
}
