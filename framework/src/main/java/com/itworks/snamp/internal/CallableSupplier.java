package com.itworks.snamp.internal;

import com.google.common.base.Supplier;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ExceptionalCallable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class CallableSupplier<V> implements ExceptionalCallable<V, ExceptionPlaceholder>, Supplier<V> {
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public V call() {
        return get();
    }

    public static <V> CallableSupplier<V> create(final Supplier<V> supplier){
        return new CallableSupplier<V>() {
            @Override
            public V get() {
                return supplier.get();
            }
        };
    }
}
