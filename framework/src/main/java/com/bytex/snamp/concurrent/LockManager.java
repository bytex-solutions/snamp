package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SafeCloseable;

import java.time.Duration;
import java.util.EnumMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;

/**
 * Represents lock manager.
 * @version 2.0
 * @since 2.0
 */
public abstract class LockManager {

    LockManager(){

    }

    abstract <E extends Throwable> SafeCloseable acquireLock(final Enum<?> resourceGroup, final Acceptor<? super Lock, E> acceptor) throws E;

    /**
     * Acquires a lock.
     * @param resourceGroup Identifier of the resource group to lock.
     * @return Lock scope.
     */
    public final SafeCloseable acquireLock(final Enum<?> resourceGroup){
        return acquireLock(resourceGroup, Lock::lock);
    }

    /**
     * Acquires a lock.
     * @param resourceGroup Identifier of the resource group to lock.
     * @param timeout Lock acquisition timeout. {@literal null} means infinite timeout.
     * @return Lock scope.
     * @throws InterruptedException The current thread is interrupted.
     * @throws TimeoutException The lock cannot be obtained in the specified timeout.
     */
    public final SafeCloseable acquireLock(final Enum<?> resourceGroup, final Duration timeout) throws InterruptedException, TimeoutException{
        final Acceptor<? super Lock, Exception> locker = timeout == null ?
                Lock::lockInterruptibly :
                lock -> {
                    if (!lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS))
                        throw new TimeoutException(String.format("Lock cannot be acquired after '%s'", timeout));
                };
        try {
            return acquireLock(resourceGroup, locker);
        } catch (final InterruptedException | TimeoutException | RuntimeException e) {
            throw e;
        } catch (final Exception e){
            throw new AssertionError("Unexpected exception", e);    //should never be happened
        }
    }

    /**
     * Executes action inside of exclusive lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>           Type of result produced by action.
     * @return Result produced by action.
     */
    public final <V> V supply(final Enum<?> resourceGroup, final Supplier<? extends V> action) {
        try (final SafeCloseable ignored = acquireLock(resourceGroup)) {
            return action.get();
        }
    }

    public final <V> V supply(final Enum<?> resourceGroup, final Supplier<? extends V> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            return action.get();
        }
    }

    public final boolean supplyBool(final Enum<?> resourceGroup, final BooleanSupplier action) {
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            return action.getAsBoolean();
        }
    }

    public final boolean supplyBool(final Enum<?> resourceGroup, final BooleanSupplier action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            return action.getAsBoolean();
        }
    }

    public final int supplyInt(final Enum<?> resourceGroup, final IntSupplier action) {
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            return action.getAsInt();
        }
    }

    public final int supplyInt(final Enum<?> resourceGroup, final IntSupplier action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            return action.getAsInt();
        }
    }

    public final <I, O> O apply(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) {
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            return action.apply(input);
        }
    }

    public final <I, O> O apply(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            return action.apply(input);
        }
    }

    public final <I, E extends Throwable> void accept(final Enum<?> resourceGroup, final I input, final Acceptor<? super I, E> action) throws E {
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            action.accept(input);
        }
    }

    public final <I, E extends Throwable> void accept(final Enum<?> resourceGroup, final I input, final Acceptor<? super I, E> action, final Duration timeout) throws E, TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            action.accept(input);
        }
    }

    public final <I1, I2> void accept(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action){
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            action.accept(input1, input2);
        }
    }

    public final <I1, I2> void accept(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            action.accept(input1, input2);
        }
    }

    public final <I1, I2, O> O apply(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            return action.apply(input1, input2);
        }
    }

    public final <I1, I2, O> O apply(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            return action.apply(input1, input2);
        }
    }

    public final <V> V call(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireLock(resourceGroup)){
            return action.call();
        }
    }

    public final <V> V call(final Enum<?> resourceGroup, final Callable<? extends V> action, final Duration timeout) throws Exception {
        try(final SafeCloseable ignored = acquireLock(resourceGroup, timeout)){
            return action.call();
        }
    }

    public static <G extends Enum<G>> LockManager reentrantLockManager(final Class<G> resourceGroupDef){
        final class ReentrantLockScope extends ReentrantLock implements SafeCloseable{
            private static final long serialVersionUID = -1714568000375347533L;

            @Override
            public void close() {
                unlock();
            }
        }
        final EnumMap<G, ReentrantLockScope> groups = new EnumMap<>(resourceGroupDef);
        for(final G item: resourceGroupDef.getEnumConstants())
            groups.put(item, new ReentrantLockScope());
        return new LockManager() {
            @Override
            <E extends Throwable> SafeCloseable acquireLock(final Enum<?> resourceGroup, final Acceptor<? super Lock, E> acceptor) throws E {
                final ReentrantLockScope scope = groups.get(resourceGroup);
                acceptor.accept(scope);
                return scope;
            }
        };
    }
}
