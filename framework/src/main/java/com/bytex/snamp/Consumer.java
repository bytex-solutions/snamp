package com.bytex.snamp;

/**
 * Represents an operation that accepts a single input argument and returns no result.
 * @param <T> Type of the value to process.
 * @param <E> Type of the exception that occurred in the operation.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Consumer<T, E extends Throwable> {
    /**
     * Performs this operation on the given argument.
     * @param value The value to process.
     * @throws E An exception thrown by the operation.
     */
    void accept(final T value) throws E;
}
