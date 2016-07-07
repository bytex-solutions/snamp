package com.bytex.snamp;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    @SuppressWarnings("unchecked")
    static <V> ExceptionalCallable<V, ExceptionPlaceholder> fromSupplier(final Supplier<V> fn){
        final MethodType invokedType = MethodType.methodType(ExceptionalCallable.class, Supplier.class);
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "call",
                    invokedType,
                    MethodType.methodType(Object.class),
                    lookup.findVirtual(Supplier.class, "get", MethodType.methodType(Object.class)),
                    MethodType.methodType(Object.class));
            return (ExceptionalCallable<V, ExceptionPlaceholder>) site.getTarget().invoke(fn);
        } catch (final Throwable e) {
            throw new AssertionError("Supplier should be reflect without exception", e);
        }
    }

    static ExceptionalCallable<Void, ExceptionPlaceholder> fromRunnable(final Runnable fn) {
        return fromSupplier(() -> {
            fn.run();
            return null;
        });
    }

    static <V> ExceptionalCallable<V, Exception> fromCallable(final Callable<V> fn){
        return fn::call;
    }
}
