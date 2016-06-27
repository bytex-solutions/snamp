package com.bytex.snamp.concurrent;

import java.util.function.Supplier;

/**
 * Represents a container for object with lazy initialization which stores a soft reference to the object.
 */
final class LazyStrongReferencedValue<V> extends AbstractLazyValue<V, V>{

    LazyStrongReferencedValue(final Supplier<V> activator) {
        super(activator);
    }

    @Override
    void reset(final V container) {
        //nothing to do
    }

    @Override
    V unref(final V container) {
        return container;
    }

    @Override
    V makeRef(final V value) {
        return value;
    }
}
