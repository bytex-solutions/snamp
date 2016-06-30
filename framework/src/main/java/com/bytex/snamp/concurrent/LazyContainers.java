package com.bytex.snamp.concurrent;

import java.util.function.Supplier;

/**
 * Provides factory methods used to instantiate {@link LazyValue} objects.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
public enum LazyContainers {
    THREAD_UNSAFE{
        @Override
        public <V> SimpleLazyValue<V> of(final Supplier<V> activator) {
            return new SimpleLazyValue<>(activator);
        }
    },

    /**
     * Thread-safe container with lazy initialization.
     */
    THREAD_SAFE {
        @Override
        public <V> LazyStrongReferencedValue<V> of(final Supplier<V> activator) {
            return new LazyStrongReferencedValue<>(activator);
        }
    },
    /**
     * Thread-safe container that store a soft reference to the initialized object.
     */
    THREAD_SAFE_SOFT_REFERENCED {
        @Override
        public <V> LazySoftReferencedValue<V> of(final Supplier<V> activator) {
            return new LazySoftReferencedValue<>(activator);
        }
    };

    /**
     * Creates a new container that provides lazy initialization.
     * @param activator Activator used to initialize object inside of container. Cannot be {@literal null}.
     * @param <V> Type of object in the container.
     * @return An object container that provides lazy initialization.
     */
    public abstract <V> LazyValue<V> of(final Supplier<V> activator);
}