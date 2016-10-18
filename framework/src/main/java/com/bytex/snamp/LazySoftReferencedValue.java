package com.bytex.snamp;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

/**
 * Represents a container for object with lazy initialization which stores a soft reference to the object.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class LazySoftReferencedValue<V> extends ThreadSafeLazyValue<V, SoftReference<V>> {

    LazySoftReferencedValue(final Supplier<V> activator) {
        super(activator);
    }

    @Override
    void reset(final SoftReference<V> container) {
        container.clear();
    }

    @Override
    V unref(final SoftReference<V> container) {
        return container.get();
    }

    @Override
    SoftReference<V> makeRef(final V value) {
        return new SoftReference<>(value);
    }
}
