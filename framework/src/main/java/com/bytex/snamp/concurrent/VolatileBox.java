package com.bytex.snamp.concurrent;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.Wrapper;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents thread-safe version of {@link com.bytex.snamp.Box}.
 * @param <T> Type of the value in container.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class VolatileBox<T> extends AtomicReference<T> implements Wrapper<T>, Supplier<T>, SafeConsumer<T> {
    private static final long serialVersionUID = 7192489973282984448L;

    /**
     * Initializes a new container.
     * @param initial The value to be placed into the container.
     */
    public VolatileBox(final T initial){
        super(initial);
    }

    /**
     * Initializes a new empty container.
     */
    public VolatileBox(){
        this(null);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param value The value to process.
     */
    @Override
    public final void accept(final T value) {
        set(value);
    }

    /**
     * Handles the wrapped object.
     * @param handler The wrapped object handler.
     * @return The wrapped object handling result.
     */
    @Override
    public final <R> R apply(final Function<T, R> handler) {
        return handler.apply(get());
    }
}
