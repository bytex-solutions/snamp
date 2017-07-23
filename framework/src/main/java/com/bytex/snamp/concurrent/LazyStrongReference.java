package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of Initialization-On-Demand pattern
 * applied to fields.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see LazySoftReference
 */
@ThreadSafe
final class LazyStrongReference<V> extends AtomicReference<V> implements LazyReference<V> {
    private static final long serialVersionUID = 813414681121113370L;

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public void accept(final V newValue) {
        set(newValue);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        set(null);
    }

    @Override
    public V getValue() {
        return get();
    }
}
