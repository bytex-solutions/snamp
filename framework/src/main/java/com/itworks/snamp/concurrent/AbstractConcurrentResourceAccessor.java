package com.itworks.snamp.concurrent;

import com.google.common.base.Function;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.Wrapper;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.*;

/**
 * Provides a base class for organizing thread-safe access to the thread-unsafe resource.
 * <p>
 *  You should implement {@link #getResource()} method in your derived class.
 * </p>
 * @param <R> Type of the thread-unsafe resource to hold.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractConcurrentResourceAccessor<R> extends ReentrantReadWriteLock implements Wrapper<R> {
    private static final long serialVersionUID = -7263363564614921684L;

    /**
     * Represents resource action that can throws an exception during execution.
     * @param <R> Type of the resource to handle.
     * @param <V> Type of the result of reading operation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface Action<R, V, E extends Throwable>{
        /**
         * Handles the resource.
         * @param resource The resource to handle.
         * @return The value obtained from the specified resource.
         * @throws E An exception that can be raised by action.
         */
        V invoke(final R resource) throws E;
    }

    /**
     * Represents resource action that cannot throws an exception during execution.
     * @param <R> Type of the resource to handle.
     * @param <V> Type of the resource handling.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface ConsistentAction<R, V> extends Action<R, V, ExceptionPlaceholder> {
        /**
         * Handles the resource.
         * @param resource The resource to handle.
         * @return The value obtained from the specified resource.
         */
        @Override
        V invoke(final R resource);
    }

    /**
     * Initializes a new concurrent access coordinator.
     */
    protected AbstractConcurrentResourceAccessor(){
    }

    /**
     * Provides unsafe access to the resource.
     * @param handler The wrapped object handler.
     * @param <RESULT> Type of the resource processing result.
     * @return The resource processing result.
     */
    @Override
    public <RESULT> RESULT handle(final Function<R, RESULT> handler){
        return handler != null ? handler.apply(getResource()) : null;
    }

    /**
     * Returns the resource.
     * @return The resource to synchronize.
     */
    protected abstract R getResource();

    /**
     * Provides consistent read on the resource.
     * <p>
     *     This operation acquires read lock (may be infinitely in time) on the resource.
     * </p>
     * @param reader The resource reader.
     * @param <V> Type of the resource reading value operation.
     * @return The value obtained from the resource.
     */
    public final <V> V read(final ConsistentAction<R, V> reader){
        return read((Action<R, V, ExceptionPlaceholder>)reader);
    }

    /**
     * Provides consistent read on the resource.
     * <p>
     *     This operation acquires read lock on the resource.
     * </p>
     * @param reader The resource reader.
     * @param readTimeout Timeout value used for acquiring read lock.
     * @param <V> Type of the resource reading value operation.
     * @return The value obtained from the resource.
     * @throws java.util.concurrent.TimeoutException Read lock cannot be acquired in the specified time.
     */
    public final <V> V read(final ConsistentAction<R, V> reader, final TimeSpan readTimeout) throws TimeoutException{
        return read((Action<R, V, ExceptionPlaceholder>)reader, readTimeout);
    }

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
    public final <V, E extends Throwable> V read(final Action<R, V, E> reader) throws E{
        if(reader == null) return null;
        final Lock rl = readLock();
        rl.lock();
        try{
            return reader.invoke(getResource());
        }
        finally {
            rl.unlock();
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
     * @throws java.util.concurrent.TimeoutException Read lock cannot be acquired in the specified time.
     */
    public final <V, E extends Throwable> V read(final Action<R, V, E> reader, final TimeSpan readTimeout) throws E, TimeoutException{
        if(reader == null) return null;
        final Lock rl = readLock();
        if(readTimeout == TimeSpan.INFINITE) rl.lock();
        else try {
            if(!rl.tryLock(readTimeout.duration, readTimeout.unit))
                throw new TimeoutException(String.format("Read operation cannot be completed in %s time.", readTimeout));
        }
        catch (final InterruptedException e) {
            throw new TimeoutException(e.getMessage());
        }
        try{
            return reader.invoke(getResource());
        }
        finally {
            rl.unlock();
        }
    }

    /**
     * Provides consistent write on the resource.
     * <p>
     *     This operation acquires write lock (may be infinitely in time) on the resource.
     * </p>
     * @param writer The resource writer.
     * @param <O> Type of the resource writing operation.
     * @return The value obtained from the resource.
     */
    public final <O> O write(final ConsistentAction<R, O> writer){
        return write((Action<R, O, ExceptionPlaceholder>)writer);
    }

    /**
     * Provides consistent write on the resource.
     * <p>
     *     This operation acquires write lock on the resource.
     * </p>
     * @param writer The resource writer.
     * @param <O> Type of the resource writing operation.
     * @return The value obtained from the resource.
     * @throws java.util.concurrent.TimeoutException Write lock cannot be acquired in the specified time.
     */
    public final <O> O write(final ConsistentAction<R, O> writer, final TimeSpan writeTimeout) throws TimeoutException{
        return write((Action<R, O, ExceptionPlaceholder>)writer, writeTimeout);
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
    public final <O, E extends Throwable> O write(final Action<R, O, E> writer) throws E{
        if(writer == null) return null;
        final Lock wl = writeLock();
        wl.lock();
        try{
            return writer.invoke(getResource());
        }
        finally {
            wl.unlock();
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
     * @throws java.util.concurrent.TimeoutException Write lock cannot be acquired in the specified time.
     */
    public final <O, E extends Throwable> O write(final Action<R, O, E> writer, final TimeSpan writeTimeout) throws E, TimeoutException{
        if(writer == null) return null;
        final Lock wl = writeLock();
        if(writeTimeout == TimeSpan.INFINITE) wl.lock();
        else try {
            if(!wl.tryLock(writeTimeout.duration, writeTimeout.unit))
                throw new TimeoutException(String.format("Write operation cannot be completed in %s time.", writeTimeout));
        }
        catch (final InterruptedException e) {
            throw new TimeoutException(e.getMessage());
        }
        try{
            return writer.invoke(getResource());
        }
        finally {
            wl.unlock();
        }
    }
}
