package com.itworks.snamp.concurrent;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.TimeSpan;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents an abstract class for all thread-safe objects.
 * <p>
 *     This class provides special methods that helps to synchronize
 *     the access to the fields. The fields may be grouped into the sections
 *     with individual read/write locks.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ThreadSafeObject {
    /**
     * Represents lock scope. This class cannot be inherited or instantiated
     * directly from your code.
     *
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static final class LockScope implements AutoCloseable {
        private final Lock locker;

        private LockScope(final Lock locker) {
            this.locker = locker;
        }

        @Override
        public void close() {
            locker.unlock();
        }
    }

    /**
     * Represents an enum that describes the single resource group.
     *
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    protected static enum SingleResourceGroup {
        /**
         * Represents a single resource group.
         */
        INSTANCE;
    }

    private final ImmutableMap<Enum<?>, ReadWriteLock> resourceGroups;

    private ThreadSafeObject(final EnumSet<?> groups) {
        switch (groups.size()) {
            case 0:
                throw new IllegalArgumentException("Set is empty.");
            case 1:
                resourceGroups = ImmutableMap.<Enum<?>, ReadWriteLock>of(groups.iterator().next(), new ReentrantReadWriteLock());
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

    private Lock getWriteLock(final Enum<?> group) {
        final ReadWriteLock lock = resourceGroups.get(group);
        if (lock == null)
            throw createInvalidSectionException(group);
        else return lock.writeLock();
    }

    private Lock getReadLock(final Enum<?> group) {
        final ReadWriteLock lock = resourceGroups.get(group);
        if (lock == null)
            throw createInvalidSectionException(group);
        else return lock.readLock();
    }

    /**
     * Acquires write lock for the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to lock.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final void beginWrite(final Enum<?> resourceGroup) {
        getWriteLock(resourceGroup).lock();
    }

    protected final boolean tryBeginWrite(final Enum<?> resourceGroup, TimeSpan timeout) throws InterruptedException {
        timeout = Objects.requireNonNull(timeout, "timeout is null")
                .convert(TimeUnit.MILLISECONDS);
        return getWriteLock(resourceGroup).tryLock(timeout.duration, timeout.unit);
    }

    protected final boolean tryBeginWrite(final TimeSpan timeout) throws InterruptedException {
        return tryBeginWrite(SingleResourceGroup.INSTANCE, timeout);
    }

    /**
     * Acquires write lock for the singleton resource group.
     *
     * @throws java.lang.IllegalArgumentException This class is not instantiated with {@link #ThreadSafeObject()} constructor.
     * @see ThreadSafeObject.SingleResourceGroup
     */
    protected final void beginWrite() {
        beginWrite(SingleResourceGroup.INSTANCE);
    }

    /**
     * Acquires write lock for the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to lock.
     * @throws java.lang.InterruptedException     The waiting thread will be interrupted by another thread.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final void beginWriteInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        getWriteLock(resourceGroup).lockInterruptibly();
    }

    /**
     * Acquires write lock for the singleton resource group.
     *
     * @throws java.lang.InterruptedException     The waiting thread will be interrupted by another thread.
     * @throws java.lang.IllegalArgumentException This class is not instantiated with {@link #ThreadSafeObject()} constructor.
     */
    protected final void beginWriteInterruptibly() throws InterruptedException {
        beginWriteInterruptibly(SingleResourceGroup.INSTANCE);
    }

    /**
     * Acquires write lock for the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to lock.
     * @return A new object that helps control the lexical scope of the lock.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final LockScope scopeWrite(final Enum<?> resourceGroup) {
        final Lock wl = getWriteLock(resourceGroup);
        wl.lock();
        return new LockScope(wl);
    }

    protected final LockScope scopeWrite() {
        return scopeWrite(SingleResourceGroup.INSTANCE);
    }

    /**
     * Acquires write lock for the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to lock.
     * @return A new object that helps control the lexical scope of the lock.
     * @throws java.lang.InterruptedException     The waiting thread will be interrupted by another thread.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final LockScope scopeWriteInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        final Lock wl = getWriteLock(resourceGroup);
        wl.lockInterruptibly();
        return new LockScope(wl);
    }

    protected final LockScope scopeWriteInterruptibly() throws InterruptedException {
        return scopeWriteInterruptibly(SingleResourceGroup.INSTANCE);
    }

    /**
     * Releases write lock associated with the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to unlock.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final void endWrite(final Enum<?> resourceGroup) {
        getWriteLock(resourceGroup).unlock();
    }

    protected final void endWrite() {
        endWrite(SingleResourceGroup.INSTANCE);
    }

    protected final void beginRead(final Enum<?> resourceGroup) {
        getReadLock(resourceGroup).lock();
    }

    protected final boolean tryBeginRead(final Enum<?> resourceGroup, TimeSpan timeout) throws InterruptedException {
        timeout = Objects.requireNonNull(timeout, "timeout is null")
                .convert(TimeUnit.MILLISECONDS);
        return getReadLock(resourceGroup).tryLock(timeout.duration, timeout.unit);
    }

    protected final boolean tryBeginRead(final TimeSpan timeout) throws InterruptedException {
        return tryBeginRead(SingleResourceGroup.INSTANCE, timeout);
    }

    protected final void beginRead() {
        beginRead(SingleResourceGroup.INSTANCE);
    }

    protected final void beginReadInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        getReadLock(resourceGroup).lockInterruptibly();
    }

    protected final void beginReadInterruptibly() throws InterruptedException {
        beginReadInterruptibly(SingleResourceGroup.INSTANCE);
    }

    protected final LockScope scopeRead(final Enum<?> resourceGroup) {
        final Lock rl = getReadLock(resourceGroup);
        rl.lock();
        return new LockScope(rl);
    }

    protected final LockScope scopeRead() {
        return scopeRead(SingleResourceGroup.INSTANCE);
    }

    protected final LockScope scopeReadInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        final Lock wl = getReadLock(resourceGroup);
        wl.lockInterruptibly();
        return new LockScope(wl);
    }

    protected final LockScope scopeReadInterruptibly() throws InterruptedException {
        return scopeReadInterruptibly(SingleResourceGroup.INSTANCE);
    }

    protected final void endRead(final Enum<?> resourceGroup) {
        getReadLock(resourceGroup).unlock();
    }

    protected final void endRead() {
        endRead(SingleResourceGroup.INSTANCE);
    }
}