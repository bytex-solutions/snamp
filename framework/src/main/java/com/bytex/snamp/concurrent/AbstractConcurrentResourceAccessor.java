package com.bytex.snamp.concurrent;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.Wrapper;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Provides a base class for organizing thread-safe access to the thread-unsafe resource.
 * <p>
 *  You should implement {@link #getResource()} method in your derived class.
 * </p>
 * @param <R> Type of the thread-unsafe resource to hold.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public abstract class AbstractConcurrentResourceAccessor<R> extends ThreadSafeObject implements Wrapper<R>, Serializable {
    private static final long serialVersionUID = -7263363564614921684L;

    /**
     * Represents resource action that can throws an exception during execution.
     * @param <R> Type of the resource to handle.
     * @param <V> Type of the result of reading operation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    public interface Action<R, V, E extends Throwable>{
        /**
         * Handles the resource.
         * @param resource The resource to handle.
         * @return The value obtained from the specified resource.
         * @throws E An exception that can be raised by action.
         */
        V apply(final R resource) throws E;
    }

    /**
     * Initializes a new concurrent access coordinator.
     */
    protected AbstractConcurrentResourceAccessor(){
    }

    /**
     * Provides unsafe access to the resource.
     * @param handler The wrapped object handler.
     * @param <O> Type of the resource processing result.
     * @return The resource processing result.
     */
    @Override
    public <O> O apply(final Function<R, O> handler) {
        if (handler == null) return null;
        try (final SafeCloseable ignored = acquireReadLock(SingleResourceGroup.INSTANCE)) {
            return handler.apply(getResource());
        }
    }

    /**
     * Returns the resource.
     * @return The resource to synchronize.
     */
    protected abstract R getResource();

    /**
     * Provides inconsistent read on the resource.
     * <p>
     *    This operation acquires read lock (may be infinitely in time) on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @param <E> Type of the exception that can be raised by reader.
     * @return The reading operation result.
     * @throws E Type of the exception that can be raised by reader.
     */
    public final <V, E extends Throwable> V read(final Action<? super R, ? extends V, E> reader) throws E {
        if (reader == null) return null;
        try (final SafeCloseable ignored = acquireReadLock(SingleResourceGroup.INSTANCE)) {
            return reader.apply(getResource());
        }
    }

    /**
     * Provides inconsistent invoke on the resource.
     * <p>
     *    This operation acquires read lock (may be infinitely in time) on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @param <E> Type of the exception that can be raised by reader.
     * @return The reading operation result.
     * @throws E Type of the exception that can be raised by reader.
     * @throws TimeoutException Read lock cannot be acquired in the specified time.
     * @throws InterruptedException Synchronization interrupted.
     */
    public final <V, E extends Throwable> V read(final Action<? super R, ? extends V, E> reader, final Duration readTimeout) throws E, TimeoutException, InterruptedException {
        if(reader == null) return null;
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(SingleResourceGroup.INSTANCE, readTimeout)){
            return reader.apply(getResource());
        }
    }

    /**
     * Provides inconsistent write on the resource.
     * <p>
     *     This operation acquires write lock (may be infinitely in time) on the resource.
     * </p>
     * @param writer The resource writer.
     * @param <O> Type of the resource writing operation.
     * @param <E> An exception that can be raised by reader.
     * @return The value obtained from the resource.
     * @throws E An exception that can be raised by reader.
     */
    public final <O, E extends Throwable> O write(final Action<? super R, ? extends O, E> writer) throws E{
        if(writer == null) return null;
        try(final SafeCloseable ignored = acquireWriteLock(SingleResourceGroup.INSTANCE)){
            return writer.apply(getResource());
        }
    }
    /**
     * Provides inconsistent write on the resource.
     * <p>
     *     This operation acquires write lock on the resource.
     * </p>
     * @param writer The resource writer.
     * @param <O> Type of the resource writing operation.
     * @param <E> An exception that can be raised by reader.
     * @return The value obtained from the resource.
     * @throws E An exception that can be raised by reader.
     * @throws TimeoutException Write lock cannot be acquired in the specified time.
     * @throws InterruptedException Synchronization interrupted.
     */
    public final <O, E extends Throwable> O write(final Action<? super R, ? extends O, E> writer, final Duration writeTimeout) throws E, TimeoutException, InterruptedException {
        if(writer == null) return null;
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(SingleResourceGroup.INSTANCE, writeTimeout)){
            return writer.apply(getResource());
        }
    }
}
