package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import static com.google.common.base.MoreObjects.firstNonNull;

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
    public <G extends Throwable> void reset(final Acceptor<? super V, G> cleaner) throws G {
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
    public V get(final Callable<? extends V> activator) throws Exception {
        return isActivated() ? value : (value = activator.call());
    }

    @Override
    public void reset() {
        value = null;
    }

    @Override
    public Optional<V> getIfPresent() {
        return value != null ? Optional.of(value) : Optional.empty();
    }
}
