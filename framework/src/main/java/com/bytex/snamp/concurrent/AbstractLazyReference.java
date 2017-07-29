package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of soft- or weak- referenced singletons.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@ThreadSafe
abstract class AbstractLazyReference<V> extends AtomicReference<Reference<V>> implements LazyReference<V> {
    private static final long serialVersionUID = 1898537173263220348L;

    abstract Reference<V> makeRef(final V value);

    @Override
    public final V getValue() {
        final Reference<V> ref = get();
        return ref == null ? null : ref.get();
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public final void accept(final V newValue) {
        set(makeRef(newValue));
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public final void reset() {
        final Reference<?> reference = getAndSet(null);
        if (reference != null)
            reference.clear();  //help GC
    }
}
