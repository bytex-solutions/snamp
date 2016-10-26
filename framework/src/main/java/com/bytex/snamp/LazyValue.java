package com.bytex.snamp;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Represents container for object with lazy activation.
 * @param <V> Type of value provided by container.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface LazyValue<V> extends Supplier<V> {
    /**
     * Releases encapsulated object.
     * @param cleaner Cleanup code use to destroy object inside of this container.
     * @throws G Cleanup code causes an exception. But container must provide guarantees that a reference to encapsulated object will be released.
     */
    <G extends Throwable> void reset(final Acceptor<? super V, G> cleaner) throws G;

    /**
     * Releases encapsulated object.
     */
    void reset();

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
    V get(final Callable<? extends V> activator) throws Exception;

    /**
     * Gets value in this container if it is initialized.
     * @return Value encapsulated by this container; or none, if this container is not initialized.
     */
    Optional<V> getIfPresent();
}
