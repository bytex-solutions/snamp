package com.itworks.snamp;

import com.google.common.base.Function;

/**
 * Represents a wrapper for the specified type of object.
 * @param <T> Type of the object that is wrapped.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Wrapper<T> {
    /**
     * Handles the wrapped object.
     * <p>
     * It is not recommended to return the original wrapped object from the handler.
     * </p>
     *
     * @param handler The wrapped object handler.
     * @param <R>     Type of the wrapped object handling result.
     * @return The wrapped object handling result.
     */
    <R> R handle(final Function<T, R> handler);

}
