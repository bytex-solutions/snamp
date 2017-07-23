package com.bytex.snamp;

import java.util.Collection;
import java.util.function.Function;

/**
 * Represents an operation that accepts a single input argument and returns no result.
 * @param <T> Type of the value to process.
 * @param <E> Type of the exception that occurred in the operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
@FunctionalInterface
public interface Acceptor<T, E extends Throwable> {

    /**
     * Performs this operation on the given argument.
     * @param value The value to process.
     * @throws E An exception thrown by the operation.
     */
    void accept(final T value) throws E;

    default <I> Acceptor<? super I, E> changeConsumingType(final Function<? super I, ? extends T> transformation) throws E{
        return inp -> accept(transformation.apply(inp));
    }

    static <T, E extends Throwable> void forEachAccept(final Collection<T> c, final Acceptor<T, E> acceptor) throws E{
        for(final T item: c)
            acceptor.accept(item);
    }
}
