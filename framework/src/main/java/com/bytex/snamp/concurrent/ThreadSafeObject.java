package com.bytex.snamp.concurrent;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.SafeCloseable;
import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;

/**
 * Represents an abstract class for all thread-safe objects.
 * <p>
 *     This class provides special methods that helps to synchronize
 *     the access to the fields. The fields may be grouped into the sections
 *     with individual read/write locks.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class ThreadSafeObject {
    /**
     * Represents an enum that describes the single resource group.
     *
     * @author Roman Sakno
     * @version 1.2
     * @since 1.0
     */
    protected enum SingleResourceGroup {
        /**
         * Represents a single resource group.
         */
        INSTANCE
    }

    private final ImmutableMap<Enum<?>, ReadWriteLock> resourceGroups;

    private ThreadSafeObject(final EnumSet<?> groups) {
        switch (groups.size()) {
            case 0:
                throw new IllegalArgumentException("Empty resource groups");
            case 1:
                resourceGroups = ImmutableMap.of(groups.iterator().next(), new ReentrantReadWriteLock());
                break;
            default:
                final ImmutableMap.Builder<Enum<?>, ReadWriteLock> builder = ImmutableMap.builder();
                for (final Enum<?> g : groups)
                    builder.put(g, new ReentrantReadWriteLock());
                resourceGroups = builder.build();
        }
    }

    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G>              Enum definition.
     */
    protected <G extends Enum<G>> ThreadSafeObject(final Class<G> resourceGroupDef) {
        this(EnumSet.allOf(resourceGroupDef));
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     * @see SingleResourceGroup
     */
    protected ThreadSafeObject() {
        this(SingleResourceGroup.class);
    }

    private <E extends Throwable> SafeCloseable acquireLock(final Enum<?> resourceGroup, final boolean writeLock, final Consumer<? super Lock, E> locker) throws E {
        final ReadWriteLock lockSupport = resourceGroups.get(resourceGroup);
        if (lockSupport == null)
            throw new IllegalArgumentException(String.format("Resource group %s is not defined.", resourceGroup));
        final Lock lock = writeLock ? lockSupport.writeLock() : lockSupport.readLock();
        locker.accept(lock);
        return lock::unlock;
    }

    private SafeCloseable acquireLock(final Enum<?> resourceGroup, final boolean writeLock, final Duration timeout) throws InterruptedException, TimeoutException {
        try {
            return acquireLock(resourceGroup, writeLock, lock -> {
                if (!lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS))
                    throw new TimeoutException(String.format("Lock cannot be acquired after '%s'", timeout));
            });
        } catch (final InterruptedException | TimeoutException | RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new AssertionError("Unexpected exception", e);    //should never be happened
        }
    }

    /**
     * Acquires the write lock.
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     * @param resourceGroup Resource group identifier. Cannot be {@literal null}.
     * @return An object that can be used to release lock only.
     */
    protected final SafeCloseable acquireWriteLock(final Enum<?> resourceGroup){
        return acquireLock(resourceGroup, true, Lock::lock);
    }

    /**
     * Acquires the read lock.
     * <p>If the lock is not available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until the
     * lock has been acquired.
     * @param resourceGroup Resource group identifier. Cannot be {@literal null}.
     * @return An object that can be used to release lock only.
     */
    protected final SafeCloseable acquireReadLock(final Enum<?> resourceGroup){
        return acquireLock(resourceGroup, false, Lock::lock);
    }

    /**
     * Acquires the write lock unless the current thread is {@linkplain Thread#interrupt interrupted}.
     * @param resourceGroup Resource group identifier. Cannot be {@literal null}.
     * @return An object that can be used to release lock only.
     * @throws InterruptedException if the current thread is interrupted while acquiring the lock
     */
    protected final SafeCloseable acquireWriteLockInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        return acquireLock(resourceGroup, true, Lock::lockInterruptibly);
    }

    protected final SafeCloseable acquireWriteLockInterruptibly(final Enum<?> resourceGroup, final Duration timeout) throws InterruptedException, TimeoutException {
        return timeout == null ? acquireWriteLockInterruptibly(resourceGroup) : acquireLock(resourceGroup, true, timeout);
    }

    /**
     * Acquires the read lock unless the current thread is {@linkplain Thread#interrupt interrupted}.
     * @param resourceGroup Resource group identifier. Cannot be {@literal null}.
     * @return An object that can be used to release lock only.
     * @throws InterruptedException if the current thread is interrupted while acquiring the lock
     */
    protected final SafeCloseable acquireReadLockInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        return acquireLock(resourceGroup, false, Lock::lockInterruptibly);
    }

    protected final SafeCloseable acquireReadLockInterruptibly(final Enum<?> resourceGroup, final Duration timeout) throws InterruptedException, TimeoutException {
        return timeout == null ? acquireReadLockInterruptibly(resourceGroup) : acquireLock(resourceGroup, false, timeout);
    }

    //<editor-fold desc="write Supplier">

    /**
     * Executes action inside of exclusive lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>           Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V write(final Enum<?> resourceGroup, final Supplier<? extends V> action) {
        try (final SafeCloseable ignored = acquireWriteLock(resourceGroup)) {
            return action.get();
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     *
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>    Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V write(final Supplier<? extends V> action) {
        return write(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="write BooleanSupplier">

    protected final boolean write(final Enum<?> resourceGroup, final BooleanSupplier action) {
        try(final SafeCloseable ignored = acquireWriteLock(resourceGroup)){
            return action.getAsBoolean();
        }
    }

    protected final boolean write(final BooleanSupplier action) {
        return write(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="write IntSupplier">

    protected final int write(final Enum<?> resourceGroup, final IntSupplier action) {
        try(final SafeCloseable ignored = acquireWriteLock(resourceGroup)){
            return action.getAsInt();
        }
    }

    protected final int write(final IntSupplier action) {
        return write(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="write Function">

    /**
     * Executes action inside of exclusive lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input         An object to be passed into the action.
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I>           Type of input to be passed into the action.
     * @param <O>           Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O write(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) {
        try(final SafeCloseable ignored = acquireWriteLock(resourceGroup)){
            return action.apply(input);
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     *
     * @param input  An object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I>    Type of input to be passed into the action.
     * @param <O>    Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O write(final I input, final Function<? super I, ? extends O> action) {
        return write(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="write Consumer">

    protected final <I, E extends Throwable> void write(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E {
        try(final SafeCloseable ignored = acquireWriteLock(resourceGroup)){
             action.accept(input);
        }
    }

    protected final <I, E extends Throwable> void write(final I input, final Consumer<? super I, E> action) throws E {
        write(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="write BiFunction">

    /**
     * Executes action inside of exclusive lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input1        The first object to be passed into the action.
     * @param input2        The second object to be passed into the action.
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I1>          Type of the first input to be passed into the action.
     * @param <I2>          Type of the second input to be passed into the action.
     * @param <O>           Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O write(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        try(final SafeCloseable ignored = acquireWriteLock(resourceGroup)){
            return action.apply(input1, input2);
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     *
     * @param input1 The first object to be passed into the action.
     * @param input2 The second object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I1>   Type of the first input to be passed into the action.
     * @param <I2>   Type of the second input to be passed into the action.
     * @param <O>    Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O write(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        return write(SingleResourceGroup.INSTANCE, input1, input2, action);
    }

    //</editor-fold>

    //<editor-fold desc="write Callable">

    protected final <V> V write(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireWriteLock(resourceGroup)){
            return action.call();
        }
    }

    protected final <V> V write(final Callable<? extends V> action) throws Exception {
        return write(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly Supplier">

    /**
     * Executes action inside of exclusive lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>           Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <V> V writeInterruptibly(final Enum<?> resourceGroup, final Supplier<? extends V> action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            return action.get();
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     *
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>    Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <V> V writeInterruptibly(final Supplier<? extends V> action) throws InterruptedException {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly BooleanSupplier">

    protected final boolean writeInterruptibly(final Enum<?> resourceGroup, final BooleanSupplier action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            return action.getAsBoolean();
        }
    }

    protected final boolean writeInterruptibly(final BooleanSupplier action) throws InterruptedException {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly IntSupplier">

    protected final int writeInterruptibly(final Enum<?> resourceGroup, final IntSupplier action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            return action.getAsInt();
        }
    }

    protected final int writeInterruptibly(final IntSupplier action) throws InterruptedException {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly Function">

    protected final <I, O> O writeInterruptibly(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            return action.apply(input);
        }
    }

    protected final <I, O> O writeInterruptibly(final I input, final Function<? super I, ? extends O> action) throws InterruptedException {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly Consumer">

    protected final <I, E extends Throwable> void writeInterruptibly(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E, InterruptedException {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            action.accept(input);
        }
    }

    protected final <I, E extends Throwable> void writeInterruptibly(final I input, final Consumer<? super I, E> action) throws E, InterruptedException {
        writeInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly BiFunction">

    protected final <I1, I2, O> O writeInterruptibly(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            return action.apply(input1, input2);
        }
    }

    protected final <I1, I2, O> O writeInterruptibly(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) throws InterruptedException {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, input1, input2, action);
    }

    //</editor-fold>

    //<editor-fold desc="writeInterruptibly Callable">

    protected final <V> V writeInterruptibly(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireWriteLockInterruptibly(resourceGroup)){
            return action.call();
        }
    }

    protected final <V> V writeInterruptibly(final Callable<? extends V> action) throws Exception {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="read Supplier">

    /**
     * Executes action inside of read lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action        Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <V>           Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V read(final Enum<?> resourceGroup, final Supplier<? extends V> action) {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            return action.get();
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     *
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>    Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V read(final Supplier<? extends V> action) {
        return read(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="read BooleanSupplier">

    protected final boolean read(final Enum<?> resourceGroup, final BooleanSupplier action) {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            return action.getAsBoolean();
        }
    }

    protected final boolean read(final BooleanSupplier action) {
        return read(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="read IntSupplier">

    protected final int read(final Enum<?> resourceGroup, final IntSupplier action) {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            return action.getAsInt();
        }
    }

    protected final int read(final IntSupplier action) {
        return read(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="read Function">

    /**
     * Executes action inside of read lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input         An object to be passed into the action.
     * @param action        Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I>           Type of input to be passed into the action.
     * @param <O>           Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O read(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            return action.apply(input);
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     *
     * @param input  An object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I>    Type of input to be passed into the action.
     * @param <O>    Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O read(final I input, final Function<? super I, ? extends O> action) {
        return read(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="read Consumer">

    protected final <I, E extends Throwable> void read(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            action.accept(input);
        }
    }

    protected final <I, E extends Throwable> void read(final I input, final Consumer<? super I, E> action) throws E {
        read(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="read Callable">

    protected final <V> V read(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            return action.call();
        }
    }

    protected final <V> V read(final Callable<? extends V> action) throws Exception {
        return read(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="read BiFunction">

    /**
     * Executes action inside of read lock.
     *
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input1        The first object to be passed into the action.
     * @param input2        The second object to be passed into the action.
     * @param action        Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I1>          Type of the first input to be passed into the action.
     * @param <I2>          Type of the second input to be passed into the action.
     * @param <O>           Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O read(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        try(final SafeCloseable ignored = acquireReadLock(resourceGroup)){
            return action.apply(input1, input2);
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     *
     * @param input1 The first object to be passed into the action.
     * @param input2 The second object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I1>   Type of the first input to be passed into the action.
     * @param <I2>   Type of the second input to be passed into the action.
     * @param <O>    Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O read(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        return read(SingleResourceGroup.INSTANCE, input1, input2, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly Supplier">

    protected final <V> V readInterruptibly(final Enum<?> resourceGroup, final Supplier<? extends V> action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            return action.get();
        }
    }

    protected final <V> V readInterruptibly(final Supplier<? extends V> action) throws InterruptedException {
        return readInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly BooleanSupplier">

    protected final boolean readInterruptibly(final Enum<?> resourceGroup, final BooleanSupplier action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            return action.getAsBoolean();
        }
    }

    protected final boolean readInterruptibly(final BooleanSupplier action) throws InterruptedException {
        return readInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly IntSupplier">

    protected final int readInterruptibly(final Enum<?> resourceGroup, final IntSupplier action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            return action.getAsInt();
        }
    }

    protected final int readInterruptibly(final IntSupplier action) throws InterruptedException {
        return readInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly Function">

    protected final <I, O> O readInterruptibly(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            return action.apply(input);
        }
    }

    protected final <I, O> O readInterruptibly(final I input, final Function<? super I, ? extends O> action) throws InterruptedException {
        return readInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly Consumer">

    protected final <I, E extends Throwable> void readInterruptibly(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E, InterruptedException {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            action.accept(input);
        }
    }

    protected final <I, E extends Throwable> void readInterruptibly(final I input, final Consumer<? super I, E> action) throws E, InterruptedException {
        readInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly Callable">

    protected final <V> V readInterruptibly(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            return action.call();
        }
    }

    protected final <V> V readInterruptibly(final Callable<? extends V> action) throws Exception {
        return readInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    //</editor-fold>

    //<editor-fold desc="readInterruptibly BiFunction">

    protected final <I1, I2, O> O readInterruptibly(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) throws InterruptedException {
        try(final SafeCloseable ignored = acquireReadLockInterruptibly(resourceGroup)){
            return action.apply(input1, input2);
        }
    }

    protected final <I1, I2, O> O readInterruptibly(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) throws InterruptedException {
        return readInterruptibly(SingleResourceGroup.INSTANCE, input1, input2, action);
    }

    //</editor-fold>
}