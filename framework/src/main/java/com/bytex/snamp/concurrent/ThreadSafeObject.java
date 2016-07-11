package com.bytex.snamp.concurrent;

import com.bytex.snamp.Consumer;
import com.bytex.snamp.SafeCloseable;
import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * @param <G> Enum definition.
     */
    protected <G extends Enum<G>> ThreadSafeObject(final Class<G> resourceGroupDef) {
        this(EnumSet.allOf(resourceGroupDef));
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     */
    protected ThreadSafeObject() {
        this(SingleResourceGroup.class);
    }

    private static IllegalArgumentException createInvalidSectionException(final Enum<?> unknownSection) {
        return new IllegalArgumentException(String.format("Resource group %s is not defined.", unknownSection));
    }

    private Lock getLock(final Enum<?> group, final boolean writeLock) {
        final ReadWriteLock lock = resourceGroups.get(group);
        if (lock == null)
            throw createInvalidSectionException(group);
        else
            return writeLock ? lock.writeLock() : lock.readLock();
    }

    private <E extends Throwable> Lock acquireLock(final Enum<?> resourceGroup, final boolean writeLock, final Consumer<? super Lock, E> locker) throws E{
        final Lock scope = getLock(resourceGroup, writeLock);
        locker.accept(scope);
        return scope;
    }

    /**
     * Executes action inside of exclusive lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V write(final Enum<?> resourceGroup, final Supplier<? extends V> action){
        final Lock scope = acquireLock(resourceGroup, true, Lock::lock);
        try{
            return action.get();
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V write(final Supplier<? extends V> action){
        return write(SingleResourceGroup.INSTANCE, action);
    }

    /**
     * Executes action inside of exclusive lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O write(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action){
        final Lock scope = acquireLock(resourceGroup, true, Lock::lock);
        try{
            return action.apply(input);
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O write(final I input, final Function<? super I, ? extends O> action) {
        return write(SingleResourceGroup.INSTANCE, input, action);
    }

    protected final <I, E extends Throwable> void write(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E{
        final Lock scope = acquireLock(resourceGroup, true, Lock::lock);
        try{
             action.accept(input);
        } finally {
            scope.unlock();
        }
    }

    protected final <I, E extends Throwable> void write(final I input, final Consumer<? super I, E> action) throws E {
         write(SingleResourceGroup.INSTANCE, input, action);
    }

    /**
     * Executes action inside of exclusive lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input1 The first object to be passed into the action.
     * @param input2 The second object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I1> Type of the first input to be passed into the action.
     * @param <I2> Type of the second input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O write(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action){
        final Lock scope = acquireLock(resourceGroup, true, Lock::lock);
        try{
            return action.apply(input1, input2);
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     * @param input1 The first object to be passed into the action.
     * @param input2 The second object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I1> Type of the first input to be passed into the action.
     * @param <I2> Type of the second input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O write(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        return write(SingleResourceGroup.INSTANCE, input1, input2, action);
    }

    /**
     * Executes action inside of exclusive lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <V> V writeInterruptibly(final Enum<?> resourceGroup, final Supplier<? extends V> action) throws InterruptedException{
        final Lock scope = acquireLock(resourceGroup, true, Lock::lockInterruptibly);
        try{
            return action.get();
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <V> V writeInterruptibly(final Supplier<? extends V> action) throws InterruptedException{
        return writeInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    /**
     * Executes action inside of exclusive lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <I, O> O writeInterruptibly(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) throws InterruptedException {
        final Lock scope = acquireLock(resourceGroup, true, Lock::lockInterruptibly);
        try {
            return action.apply(input);
        } finally {
            scope.unlock();
        }
    }

    protected final <V> V writeInterruptibly(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        final Lock scope = acquireLock(resourceGroup, true, Lock::lockInterruptibly);
        try{
            return action.call();
        } finally {
            scope.unlock();
        }
    }

    protected final <V> V writeInterruptibly(final Callable<? extends V> action) throws Exception {
        return writeInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    /**
     * Executes action inside of exclusive lock on default resource group.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <I, O> O writeInterruptibly(final I input, final Function<? super I, ? extends O> action) throws InterruptedException{
        return writeInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    /**
     * Executes action inside of read lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V read(final Enum<?> resourceGroup, final Supplier<? extends V> action){
        final Lock scope = acquireLock(resourceGroup, false, Lock::lock);
        try{
            return action.get();
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     * @param action Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <V> V read(final Supplier<? extends V> action){
        return read(SingleResourceGroup.INSTANCE, action);
    }



    /**
     * Executes action inside of read lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O read(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action){
        final Lock scope = acquireLock(resourceGroup, false, Lock::lock);
        try{
            return action.apply(input);
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I, O> O read(final I input, final Function<? super I, ? extends O> action) {
        return read(SingleResourceGroup.INSTANCE, input, action);
    }

    /**
     * Executes action inside of exclusive lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <V> V readInterruptibly(final Enum<?> resourceGroup, final Supplier<? extends V> action) throws InterruptedException{
        final Lock scope = acquireLock(resourceGroup, false, Lock::lockInterruptibly);
        try{
            return action.get();
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <V> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <V> V readInterruptibly(final Supplier<? extends V> action) throws InterruptedException{
        return readInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    /**
     * Executes action inside of read lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <I, O> O readInterruptibly(final Enum<?> resourceGroup, final I input, final Function<? super I, ? extends O> action) throws InterruptedException {
        final Lock scope = acquireLock(resourceGroup, false, Lock::lockInterruptibly);
        try {
            return action.apply(input);
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     * @param input An object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I> Type of input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     * @throws InterruptedException This method was interrupted by another thread.
     */
    protected final <I, O> O readInterruptibly(final I input, final Function<? super I, ? extends O> action) throws InterruptedException{
        return readInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    /**
     * Executes action inside of read lock.
     * @param resourceGroup The identifier of the resource group to lock. Cannot be {@literal null}.
     * @param input1 The first object to be passed into the action.
     * @param input2 The second object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I1> Type of the first input to be passed into the action.
     * @param <I2> Type of the second input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O read(final Enum<?> resourceGroup, final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action){
        final Lock scope = acquireLock(resourceGroup, false, Lock::lock);
        try{
            return action.apply(input1, input2);
        } finally {
            scope.unlock();
        }
    }

    /**
     * Executes action inside of read lock on default resource group.
     * @param input1 The first object to be passed into the action.
     * @param input2 The second object to be passed into the action.
     * @param action Action to execute inside of read lock. Cannot be {@literal null}.
     * @param <I1> Type of the first input to be passed into the action.
     * @param <I2> Type of the second input to be passed into the action.
     * @param <O> Type of result produced by action.
     * @return Result produced by action.
     */
    protected final <I1, I2, O> O read(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        return read(SingleResourceGroup.INSTANCE, input1, input2, action);
    }

    protected final <V> V readInterruptibly(final Enum<?> resourceGroup, final Callable<? extends V> action) throws Exception {
        final Lock scope = acquireLock(resourceGroup, false, Lock::lockInterruptibly);
        try{
            return action.call();
        } finally {
            scope.unlock();
        }
    }

    protected final <V> V readInterruptibly(final Callable<? extends V> action) throws Exception {
        return readInterruptibly(SingleResourceGroup.INSTANCE, action);
    }

    protected final <I, E extends Throwable> void readInterruptibly(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E, InterruptedException{
        final Lock scope = acquireLock(resourceGroup, false, Lock::lockInterruptibly);
        try{
            action.accept(input);
        } finally {
            scope.unlock();
        }
    }

    protected final <I, E extends Throwable> void readInterruptibly(final I input, final Consumer<? super I, E> action) throws E, InterruptedException {
        readInterruptibly(SingleResourceGroup.INSTANCE, input, action);
    }

    protected final <I, E extends Throwable> void read(final Enum<?> resourceGroup, final I input, final Consumer<? super I, E> action) throws E{
        final Lock scope = acquireLock(resourceGroup, false, Lock::lock);
        try{
            action.accept(input);
        } finally {
            scope.unlock();
        }
    }

    protected final <I, E extends Throwable> void read(final I input, final Consumer<? super I, E> action) throws E {
        read(SingleResourceGroup.INSTANCE, input, action);
    }
}