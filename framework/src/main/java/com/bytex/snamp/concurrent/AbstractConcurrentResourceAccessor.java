package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.SafeCloseable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Externalizable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Provides a base class for organizing thread-safe access to the thread-unsafe resource.
 * <p>
 *  You should implement {@link #getResource()} method in your derived class.
 * </p>
 * @param <R> Type of the thread-unsafe resource to hold.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
@ThreadSafe
public abstract class AbstractConcurrentResourceAccessor<R> implements Externalizable {
    private static final long serialVersionUID = -7263363564614921684L;

    /**
     * Represents resource action that can throws an exception during execution.
     * @param <R> Type of the resource to handle.
     * @param <V> Type of the result of reading operation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    @FunctionalInterface
    public interface Action<R, V, E extends Throwable>{
        /**
         * Handles the resource.
         * @param resource The resource to handle.
         * @return The value obtained from the specified resource.
         * @throws E An exception that can be raised by action.
         */
        V apply(final R resource) throws E;

        /**
         * Converts acceptor into thread-safe action.
         * @param acceptor Acceptor to be converted into action. Cannot be {@literal null}.
         * @param <R> Type of consumed resource.
         * @param <E> Type of exception that can be thrown by acceptor.
         * @return A new action implementation.
         * @since 2.1
         */
        static <R, E extends Throwable> Action<R, Void, E> fromAcceptor(@Nonnull final Acceptor<? super R, E> acceptor){
            return resource -> {
                acceptor.accept(resource);
                return null;
            };
        }

        /**
         * Converts consumer into thread-safe action.
         * @param consumer Acceptor to be converted into action. Cannot be {@literal null}.
         * @param <R> Type of consumed resource.
         * @return A new action implementation.
         * @since 2.1
         */
        static <R> Action<R, Void, ExceptionPlaceholder> fromConsumer(@Nonnull final Consumer<? super R> consumer){
            return resource -> {
                consumer.accept(resource);
                return null;
            };
        }
    }

    /**
     * Represents read lock for thread-unsafe resource.
     */
    protected final LockDecorator readLock;
    /**
     * Represents write lock for thread-unsafe resource.
     */
    protected final LockDecorator writeLock;

    /**
     * Initializes a new concurrent access coordinator.
     */
    protected AbstractConcurrentResourceAccessor(){
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = LockDecorator.readLock(rwLock);
        writeLock = LockDecorator.writeLock(rwLock);
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
        try (final SafeCloseable ignored = readLock.acquireLock()) {
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
        try (final SafeCloseable ignored = readLock.acquireLock(readTimeout)) {
            return reader.apply(getResource());
        }
    }

    public final <V, E extends Throwable> Optional<V> read(final Action<? super R, ? extends V, E> reader, final Duration readTimeout, final Consumer<? super Throwable> errorHandler){
        V result;
        try (final SafeCloseable ignored = readLock.acquireLock(readTimeout)) {
            result = reader.apply(getResource());
        } catch (final Error e){
            throw e;
        } catch (final Throwable e){
            errorHandler.accept(e);
            result = null;
        }
        return Optional.ofNullable(result);
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
        try(final SafeCloseable ignored = writeLock.acquireLock()){
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
        try(final SafeCloseable ignored = writeLock.acquireLock(writeTimeout)){
            return writer.apply(getResource());
        }
    }

    public final <O, E extends Throwable> Optional<O> write(final Action<? super R, ? extends O, E> writer, final Duration writeTimeout, final Consumer<? super Throwable> errorHandler) {
        O result;
        try (final SafeCloseable ignored = writeLock.acquireLock(writeTimeout)) {
            result = writer.apply(getResource());
        } catch (final Error e) {
            throw e;
        } catch (final Throwable e) {
            errorHandler.accept(e);
            result = null;
        }
        return Optional.ofNullable(result);
    }
}
