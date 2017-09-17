package com.bytex.snamp.concurrent;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.ref.Reference;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of soft- or weak- referenced singletons.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@ThreadSafe
abstract class AbstractLazyIndirectReference<V> extends AbstractLazyReference<V> implements LazyReference<V> {
    private static final long serialVersionUID = 1898537173263220348L;
    private volatile Reference<V> reference;

    abstract Reference<V> makeRef(final V value);

    @Override
    public final V getRawValue() {
        final Reference<V> ref = reference;
        return ref == null ? null : ref.get();
    }

    /**
     * Removes value stored in the lazy reference.
     *
     * @return Value stored in the lazy reference.
     */
    @Override
    public final Optional<V> remove() {
        Reference<V> reference = this.reference;
        if (reference == null || reference.get() == null)
            return Optional.empty();
        else {
            synchronized (this) {
                reference = this.reference;
                this.reference = null;
            }
            return Optional.ofNullable(reference).map(ref -> {
                final V result = ref.get();
                ref.clear();
                return result;
            });
        }
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public final void accept(final V newValue) {
        reference = makeRef(newValue);
    }
}
