package com.bytex.snamp;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * A task that returns a result and may throw an exception.
 * @param <V> The computation result type.
 * @param <E> Type of the exception that may be produced by {@code call} method.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@FunctionalInterface
public interface ExceptionalCallable<V, E extends Exception> extends Callable<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws E if unable to compute a result
     */
    @Override
    V call() throws E;

    static <V> ExceptionalCallable<V, ExceptionPlaceholder> fromSupplier(final Supplier<V> fn){
        return new ExceptionalCallable<V, ExceptionPlaceholder>() { //DO NOT REPLACE WITH LAMDA!! Compiler can't understand this statement in lambda form
            @Override
            public V call() {
                return fn.get();
            }
        };
    }

    static <V> ExceptionalCallable<V, Exception> fromCallable(final Callable<V> fn){
        return fn::call;
    }
}
