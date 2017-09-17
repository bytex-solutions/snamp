package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a reference to object with lazy initialization.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface LazyReference<V> extends Serializable {
    V get(final Supplier<? extends V> initializer);

    <I> V get(final I input, final Function<? super I, ? extends V> initializer);

    <I1, I2> V get(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer);

    <E extends Throwable> V get(final Acceptor<? super Consumer<V>, E> initializer) throws E;

    /**
     * Gets value stored in this container.
     * @return A value stored in this container.
     * @since 2.1
     */
    Optional<V> get();

    /**
     * Removes value stored in the lazy reference.
     * @return Value stored in the lazy reference.
     * @since 2.1
     */
    Optional<V> remove();

    /**
     * Creates a new container with lazy initialization which stores strong reference to the object in container.
     * @param <V> Type of object in container.
     * @return A new instance of container.
     */
    static <V> LazyReference<V> strong(){
        return new LazyStrongReference<>();
    }

    /**
     * Creates a new container with lazy initialization which stores soft reference to the object in container.
     * @param <V> Type of object in container.
     * @return A new instance of container.
     */
    static <V> LazyReference<V> soft(){
        return new LazySoftReference<>();
    }

    /**
     * Creates a new container with lazy initialization which stores weak reference to the object in container.
     * @param <V> Type of object in container.
     * @return A new instance of container.
     */
    static <V> LazyReference<V> weak(){
        return new LazyWeakReference<>();
    }
}
