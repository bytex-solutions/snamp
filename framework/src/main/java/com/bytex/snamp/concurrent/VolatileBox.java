package com.bytex.snamp.concurrent;

import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.Wrapper;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * Retrieves an instance of the stored object or returns alternative value
     * if stored object is {@literal null}.
     * @param defval The alternative value to return.
     * @return An object stored in this box; or {@code defval} if stored object is {@literal null}.
     * @since 1.2
     */
    public final T getOrDefault(final Supplier<T> defval){
        final T value = get();
        return value == null ? defval.get() : value;
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
