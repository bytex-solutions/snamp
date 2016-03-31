package com.bytex.snamp;

import java.util.concurrent.Callable;

/**
 * A task that returns a result and may throw an exception.
 * @param <V> The computation result type.
 * @param <E> Type of the exception that may be produced by {@code call} method.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ExceptionalCallable<V, E extends Exception> extends Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws E if unable to compute a result
     */
    @Override
    V call() throws E;
}
