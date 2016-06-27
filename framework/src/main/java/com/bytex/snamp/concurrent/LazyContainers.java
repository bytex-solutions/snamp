package com.bytex.snamp.concurrent;

import java.util.function.Supplier;

/**
 * Provides factory methods used to instantiate {@link LazyValue} objects.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
public enum LazyContainers {
    /**
     * Normal container with lazy initialization.
     */
    NORMAL {
        @Override
        public <V> LazyStrongReferencedValue<V> create(final Supplier<V> activator) {
            return new LazyStrongReferencedValue<>(activator);
        }
    },
    /**
     * A container that store a soft reference to the initialized object.
     */
    SOFT_REFERENCED {
        @Override
        public <V> LazySoftReferencedValue<V> create(final Supplier<V> activator) {
            return new LazySoftReferencedValue<>(activator);
        }
    };

    /**
     * Creates a new container that provides lazy initialization.
     * @param activator Activator used to initialize object inside of container. Cannot be {@literal null}.
     * @param <V> Type of object in the container.
     * @return An object container that provides lazy initialization.
     */
    public abstract <V> LazyValue<V> create(final Supplier<V> activator);
}