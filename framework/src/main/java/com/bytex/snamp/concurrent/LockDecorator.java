package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SafeCloseable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.*;

/**
 * Represents decorator for {@link Lock}.
 * @version 2.1
 * @since 2.0
 */
public abstract class LockDecorator implements Serializable {
    private static final long serialVersionUID = -5122029652598077166L;

    /**
     * Decorates the specified lock.
     * @param lock The lock to be decorated. Cannot be {@literal null}.
     * @return Lock decorator.
     */
    public static LockDecorator of(@Nonnull final Lock lock) {
        final class DefaultLockDecorator extends LockDecorator implements SafeCloseable {
            private static final long serialVersionUID = -6283252678010708142L;

            @Override
            public SafeCloseable acquireLock() {
                lock.lock();
                return this;
            }

            @Override
            public SafeCloseable acquireLock(final Duration timeout) throws InterruptedException, TimeoutException {
                if (timeout == null)
                    lock.lockInterruptibly();
                else if (!lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS))
                    throw new TimeoutException();
                return this;
            }

            @Override
            public void close() {
                lock.unlock();
            }

            @Override
            public int hashCode() {
                return lock.hashCode();
            }

            private boolean lockEquals(final Lock other){
                return Objects.equals(lock, other);
            }

            private boolean equals(final DefaultLockDecorator other) {
                return other.lockEquals(lock);
            }

            @Override
            public boolean equals(final Object other) {
                return this == other || other instanceof DefaultLockDecorator && equals((DefaultLockDecorator) other);
            }

            @Override
            public String toString() {
                return lock.toString();
            }
        }
        return new DefaultLockDecorator();
    }

    /**
     * Decorates read lock.
     * @param lock Read/write lock. Cannot be {@literal null}.
     * @return Decorator for read lock.
     */
    @Nonnull
    public static LockDecorator readLock(@Nonnull final ReadWriteLock lock){
        return of(lock.readLock());
    }

    /**
     * Decorates write lock.
     * @param lock Read/write lock. Cannot be {@literal null}.
     * @return Decorator for write lock.
     */
    @Nonnull
    public static LockDecorator writeLock(@Nonnull final ReadWriteLock lock) {
        return of(lock.writeLock());
    }

    /**
     * Acquires a lock.
     * @return Lock scope.
     */
    public abstract SafeCloseable acquireLock();

    /**
     * Acquires a lock.
     * @param timeout Lock acquisition timeout. {@literal null} means infinite timeout.
     * @return Lock scope.
     * @throws InterruptedException The current thread is interrupted.
     * @throws TimeoutException The lock cannot be obtained in the specified timeout.
     */
    public abstract SafeCloseable acquireLock(final Duration timeout) throws InterruptedException, TimeoutException;

    /**
     * Executes action inside of exclusive lock.
     *
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>           Type of result produced by action.
     * @return Result produced by action.
     */
    public final <V> V supply(final Supplier<? extends V> action) {
        try (final SafeCloseable ignored = acquireLock()) {
            return action.get();
        }
    }

    public final <V> V supply(final Supplier<? extends V> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.get();
        }
    }

    public final boolean supplyBool(final BooleanSupplier action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.getAsBoolean();
        }
    }

    public final boolean supplyBool(final BooleanSupplier action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.getAsBoolean();
        }
    }

    public final int supplyInt(final IntSupplier action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.getAsInt();
        }
    }

    public final int supplyInt(final IntSupplier action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.getAsInt();
        }
    }

    public final  <I> double applyAsDouble(final I input, final ToDoubleFunction<? super I> action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.applyAsDouble(input);
        }
    }

    public final <I> double applyAsDouble(final I input, final ToDoubleFunction<? super I> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try (final SafeCloseable ignored = acquireLock(timeout)) {
            return action.applyAsDouble(input);
        }
    }

    public final <I, O> O apply(final I input, final Function<? super I, ? extends O> action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.apply(input);
        }
    }

    public final <I, O> O apply(final I input, final Function<? super I, ? extends O> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.apply(input);
        }
    }

    public final <I, E extends Throwable> void accept(final I input, final Acceptor<? super I, E> action) throws E {
        try(final SafeCloseable ignored = acquireLock()){
            action.accept(input);
        }
    }

    public final <I, E extends Throwable> void accept(final I input, final Acceptor<? super I, E> action, final Duration timeout) throws E, TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.accept(input);
        }
    }

    public final <I1, I2> void accept(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action){
        try(final SafeCloseable ignored = acquireLock()){
            action.accept(input1, input2);
        }
    }

    public final <I1, I2> void accept(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.accept(input1, input2);
        }
    }

    public final <I1, I2, O> O apply(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.apply(input1, input2);
        }
    }

    public final <I1, I2, O> O apply(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.apply(input1, input2);
        }
    }

    public final void run(final Runnable action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.run();
        }
    }

    public final void run(final Runnable action){
        try(final SafeCloseable ignored = acquireLock()){
            action.run();
        }
    }

    public final <V> V call(final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireLock()){
            return action.call();
        }
    }

    public final <V> V call(final Callable<? extends V> action, final Duration timeout) throws Exception {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.call();
        }
    }

    public final <I> void acceptLong(final I input1, final long input2, final ObjLongConsumer<? super I> action){
        try(final SafeCloseable ignored = acquireLock()){
            action.accept(input1, input2);
        }
    }

    public final <I> void acceptLong(final I input1, final long input2, final ObjLongConsumer<? super I> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.accept(input1, input2);
        }
    }
}
