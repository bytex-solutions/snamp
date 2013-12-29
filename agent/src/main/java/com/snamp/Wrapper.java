package com.snamp;

/**
 * Represents a wrapper for the specified type of object.
 * @param <T> Type of the object that is wrapped.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Wrapper<T> {
    /**
     * Represents wrapped object handler.
     * @param <T> Type of the wrapped object to handle.
     * @param <R> Type of the handling result.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface WrappedObjectHandler<T, R>{
        /**
         * Handles the wrapped object.
         * @param obj The wrapped object to handle.
         * @return The handling result.
         */
        public R invoke(final T obj);
    }

    /**
     * Handles the wrapped object.
     * @param handler The wrapped object handler.
     * @param <R> Type of the wrapped object handling result.
     * @return The wrapped object handling result.
     */
    public <R> R handle(final WrappedObjectHandler<T, R> handler);
}
