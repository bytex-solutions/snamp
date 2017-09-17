package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents advanced version of {@link AtomicReference} that can be used for implementation of Initialization-On-Demand pattern
 * applied to fields.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 * @see LazySoftReference
 */
@ThreadSafe
final class LazyStrongReference<V> extends AbstractLazyReference<V> {
    private static final long serialVersionUID = 813414681121113370L;
    private volatile V value;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public LazyStrongReference() {
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    @Override
    public void accept(final V newValue) {
        value = newValue;
    }

    @Nullable
    @Override
    public V getRawValue() {
        return value;
    }

    @Override
    public Optional<V> remove() {
        if (value == null)
            return Optional.empty();
        else {
            final V previous;
            synchronized (this) {
                previous = value;
                value = null;
            }
            return Optional.ofNullable(previous);
        }
    }
}
