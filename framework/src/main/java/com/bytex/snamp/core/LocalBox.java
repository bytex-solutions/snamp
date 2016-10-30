package com.bytex.snamp.core;

import com.bytex.snamp.Box;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Represents local implementation of Box cluster service.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class LocalBox extends AtomicReference<Object> implements Box<Object> {
    private static final long serialVersionUID = -4201147737679412750L;

    @Override
    public Object setIfAbsent(final Supplier<?> valueProvider) {
        Object current;
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
    public Object getOrDefault(final Supplier<?> defaultProvider) {
        final Object current = get();
        return current == null ? defaultProvider.get() : current;
    }

    @Override
    public boolean hasValue() {
        return get() != null;
    }

    @Override
    public void accept(final Object value) {
        set(value);
    }
}
