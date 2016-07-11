package com.bytex.snamp.concurrent;

import com.bytex.snamp.Consumer;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Represents abstract class of all implementations of {@link LazyValue} interface.
 * @param <V> Type of object stored in the container.
 * @param <C> Type of inner container that store the object.
 */
abstract class ThreadSafeLazyValue<V, C> implements LazyValue<V> {
    private final Supplier<? extends V> activator;
    private volatile C ref;

    ThreadSafeLazyValue(final Supplier<? extends V> activator){
        this.activator = Objects.requireNonNull(activator);
    }

    abstract void reset(final C container);

    abstract V unref(final C container);

    abstract C makeRef(final V value);

    @Override
    public final synchronized <G extends Throwable> void reset(final Consumer<? super V, G> cleanup) throws G {
        if (ref != null) {
            try {
                final V value = unref(ref);
                if (value != null)
                    cleanup.accept(value);
            } finally {
                reset(ref);
                ref = null;
            }
        }
    }

    @Override
    public final synchronized void reset() {
        if(ref != null){
            reset(ref);
        }
        ref = null;
    }

    @Override
    public final boolean isActivated() {
        final C localRef = ref;
        return localRef != null && unref(localRef) != null;
    }

    private synchronized V callSync(final Callable<? extends V> activator) throws Exception {
        V value;
        if (ref == null || (value = unref(ref)) == null)
            ref = makeRef(value = activator.call());
        return value;
    }

    @Override
    public final V get(final Callable<? extends V> activator) throws Exception {
        final C localRef = ref;
        V value;
        return localRef == null || (value = unref(localRef)) == null ? callSync(activator) : value;
    }

    private synchronized V getSync(){
        V value;
        if (ref == null || (value = unref(ref)) == null)
            ref = makeRef(value = activator.get());
        return value;
    }

    @Override
    public final V get() {
        final C localRef = ref;
        V value;
        return localRef == null || (value = unref(localRef)) == null ? getSync() : value;
    }

    @Override
    public final synchronized V getIfActivated() throws IllegalStateException {
        final C localRef = ref;
        V value;
        if (localRef == null || (value = unref(localRef)) == null)
            throw new IllegalStateException("Container is not initialized");
        else
            return value;
    }
}
