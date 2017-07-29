package com.bytex.snamp.core;

import com.bytex.snamp.Box;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents local implementation of Box cluster service.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class InMemoryBox extends AtomicReference<Serializable> implements Box<Serializable>, SharedBox {
    private static final long serialVersionUID = -4201147737679412750L;
    private final String name;

    InMemoryBox(final String name){
        this.name = name;
    }

    @Override
    public Serializable setIfAbsent(final Supplier<? extends Serializable> valueProvider) {
        Serializable current;
        do {
            current = get();
            if (current == null)
                current = valueProvider.get();
            else
                break;
        } while (!compareAndSet(null, current));
        return current;
    }

    @Override
    public void reset() {
        set(null);
    }

    @Override
    public Serializable getOrDefault(final Supplier<? extends Serializable> defaultProvider) {
        final Serializable current = get();
        return current == null ? defaultProvider.get() : current;
    }

    @Override
    public <R> Optional<R> map(final Function<? super Serializable, ? extends R> mapper) {
        return Optional.ofNullable(get()).map(mapper);
    }

    @Override
    public boolean hasValue() {
        return get() != null;
    }

    @Override
    public void accept(final Serializable value) {
        set(value);
    }

    @Override
    public String getName() {
        return name;
    }
}
