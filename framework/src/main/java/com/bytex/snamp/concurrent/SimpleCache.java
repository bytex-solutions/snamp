package com.bytex.snamp.concurrent;

import com.bytex.snamp.internal.annotations.ThreadSafe;

/**
 * Represents a single thread-safe cache.
 * @param <I> Type of the source used to initialize cache.
 * @param <O> Type of the value stored in the cache.
 * @param <E> Type of the exception that may be produced by cache initializer.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe
public abstract class SimpleCache<I, O, E extends Throwable> {
    private volatile O value = null;

    /**
     * Initializes cache.
     * @param input An input object used to initialize cache.
     * @return A new object to be placed into this cache.
     * @throws E Unable to initialize cache.
     */
    protected abstract O init(final I input) throws E;

    private synchronized O getSynchronized(final I input) throws E {
        if(value == null)
            value = init(input);
        return value;
    }

    /**
     * Gets cached value.
     * @param input An object used to initialize cache if it is necessary.
     * @return The cached object.
     * @throws E Unable to initialize cache.
     */
    public final O get(final I input) throws E{
        return value == null ? getSynchronized(input) : value;
    }

    /**
     * Sets the cached object to null and return the cached object.
     * @return The cached object.
     */
    protected final synchronized O invalidate(){
        final O result = value;
        value = null;
        return result;
    }
}