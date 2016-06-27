package com.bytex.snamp.concurrent;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.ThreadSafe;

import java.util.function.Supplier;

/**
 * Represents a thread-safe container for object with activation.
 * @param <V> Type of value provided by container.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
@ThreadSafe
public interface LazyValue<V> extends Supplier<V> {
    /**
     * Releases encapsulated object.
     * @param cleaner Cleanup code use to destroy object inside of this container.
     * @throws G Cleanup code causes an exception. But container must provide guarantees that a reference to encapsulated object will be released.
     */
    <G extends Throwable> void reset(final Consumer<? super V, G> cleaner) throws G;

    /**
     * Releases encapsulated object.
     */
    default void reset(){
        reset(value -> {});
    }

    /**
     * Determines whether object in this container already instantiated.
     * @return {@literal true}, if object in this container already activated; otherwise, {@literal false}.
     */
    boolean isActivated();

    /**
     * Gets object stored in this container and instantiate it if it is necessary.
     * @return An object stored in this container.
     */
    @Override
    V get();

    /**
     * Gets object stored in this container and instantiate it if it is necessary using supplied activator.
     * <p>
     *  If object is already activated then supplied activator will not be called.
     * @return An object stored in this container.
     */
    <E extends Exception> V get(final ExceptionalCallable<? extends V, E> activator) throws E;

    /**
     * Gets object stored in this container only if it is already instantiated.
     * @return An object stored in this container.
     * @throws IllegalStateException Container is not initialized.
     * @see #isActivated()
     */
    V getIfActivated() throws IllegalStateException;
}
