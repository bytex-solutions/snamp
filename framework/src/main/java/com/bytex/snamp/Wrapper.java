package com.bytex.snamp;

import java.util.function.Function;

/**
 * Represents a wrapper for the specified type of object.
 * @param <T> Type of the object that is wrapped.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@FunctionalInterface
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
    <R> R apply(final Function<T, R> handler);

}
