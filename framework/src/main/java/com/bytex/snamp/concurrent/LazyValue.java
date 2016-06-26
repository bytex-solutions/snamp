package com.bytex.snamp.concurrent;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents lazy value with deferred activation.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public abstract class LazyValue<T> implements Supplier<T> {
    private final Supplier<T> activator;

    private LazyValue(final Supplier<T> activator){
        this.activator = Objects.requireNonNull(activator);
    }

    /**
     * Releases encapsulated object.
     */
    public abstract void reset();

    /**
     * Determines whether object in this container already instantiated.
     * @return {@literal true}, if object in this container already activated; otherwise, {@literal false}.
     */
    public abstract boolean isActivated();

    /**
     * Gets object stored in this container and instantiate it if it is necessary.
     * @return An object stored in this container.
     */
    @Override
    public abstract T get();

    private static <T> LazyValue<T> storeAsStrongReference(final Supplier<T> activator){
        return new LazyValue<T>(activator) {
            private volatile T value;

            @Override
            public synchronized void reset() {
                value = null;
            }

            @Override
            public boolean isActivated() {
                return value != null;
            }

            private synchronized T getSync(){
                if(value == null)
                    value = super.activator.get();
                return value;
            }

            @Override
            public T get() {
                final T localVal = value;
                return localVal == null ? getSync() : localVal;
            }
        };
    }

    private static <T> LazyValue<T> storeAsSoftReference(final Supplier<T> activator){
        return new LazyValue<T>(activator) {
            private SoftReference<T> ref;

            @Override
            public synchronized void reset() {
                if(ref != null)
                    ref.clear();
                ref = null;
            }

            @Override
            public boolean isActivated() {
                final Reference<T> localRef = ref;
                return localRef != null && localRef.get() != null;
            }

            private synchronized T getSync(){
                T value;
                if(ref == null || (value = ref.get()) == null)
                    ref = new SoftReference<>(value = super.activator.get());
                return value;
            }

            @Override
            public T get() {
                final Reference<T> localRef = ref;
                T value;
                return localRef == null || (value = localRef.get()) == null ? getSync() : value;
            }
        };
    }

    public static <T> LazyValue<T> create(final Supplier<T> activator, final boolean storeAsSoftReference){
        return storeAsSoftReference ? storeAsSoftReference(activator) : storeAsStrongReference(activator);
    }
}
