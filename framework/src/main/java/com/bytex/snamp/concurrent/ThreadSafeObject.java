package com.bytex.snamp.concurrent;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.TimeSpan;
import com.google.common.collect.ImmutableMap;

import java.util.EnumSet;
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
 * @version 1.2
 * @since 1.0
 */
public abstract class ThreadSafeObject {
    /**
     * Represents lock scope.
     * @author Roman Sakno
     * @version 1.2
     * @since 1.0
     */
    public interface LockScope extends SafeCloseable, Lock {
        /**
         * Releases the lock.
         */
        @Override
        void close();
    }

    private static final class ReadLockScope extends ReentrantReadWriteLock.ReadLock implements LockScope{
        private static final long serialVersionUID = -4674488641147301623L;

        private ReadLockScope(final ReentrantReadWriteLock lock) {
            super(lock);
        }

        @Override
        public void close() {
            unlock();
        }
    }

    private static final class WriteLockScope extends ReentrantReadWriteLock.WriteLock implements LockScope{
        private static final long serialVersionUID = 8930001265056640152L;

        private WriteLockScope(final ReentrantReadWriteLock lock) {
            super(lock);
        }

        @Override
        public void close() {
            unlock();
        }
    }

    private interface ReadWriteLockSlim extends ReadWriteLock{
        @SuppressWarnings("NullableProblems")
        @Override
        LockScope readLock();

        @SuppressWarnings("NullableProblems")
        @Override
        LockScope writeLock();
    }

    private static final class ReentrantReadWriteLockSlim implements ReadWriteLockSlim{
        private final ReadLockScope readLock;
        private final WriteLockScope writeLock;

        private ReentrantReadWriteLockSlim(){
            final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
            readLock = new ReadLockScope(reentrantLock);
            writeLock = new WriteLockScope(reentrantLock);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public ReadLockScope readLock() {
            return readLock;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public WriteLockScope writeLock() {
            return writeLock;
        }
    }

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

    private final ImmutableMap<Enum<?>, ReadWriteLockSlim> resourceGroups;

    private ThreadSafeObject(final EnumSet<?> groups) {
        switch (groups.size()) {
            case 0:
                throw new IllegalArgumentException("Empty resource groups");
            case 1:
                resourceGroups = ImmutableMap.of(groups.iterator().next(), new ReentrantReadWriteLockSlim());
                break;
            default:
                final ImmutableMap.Builder<Enum<?>, ReadWriteLockSlim> builder = ImmutableMap.builder();
                for (final Enum<?> g : groups)
                    builder.put(g, new ReentrantReadWriteLockSlim());
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

    private LockScope getWriteLock(final Enum<?> group) {
        final ReadWriteLockSlim lock = resourceGroups.get(group);
        if (lock == null)
            throw createInvalidSectionException(group);
        else return lock.writeLock();
    }

    private LockScope getReadLock(final Enum<?> group) {
        final ReadWriteLockSlim lock = resourceGroups.get(group);
        if (lock == null)
            throw createInvalidSectionException(group);
        else return lock.readLock();
    }

    /**
     * Acquires write lock for the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to lock.
     * @return An object that can be used to control lock scope.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final LockScope beginWrite(final Enum<?> resourceGroup) {
        final LockScope scope = getWriteLock(resourceGroup);
        scope.lock();
        return scope;
    }

    protected final boolean tryBeginWrite(final Enum<?> resourceGroup, final TimeSpan timeout) throws InterruptedException {
        return getWriteLock(resourceGroup).tryLock(timeout.duration, timeout.unit);
    }

    protected final boolean tryBeginWrite(final TimeSpan timeout) throws InterruptedException {
        return tryBeginWrite(SingleResourceGroup.INSTANCE, timeout);
    }

    /**
     * Acquires write lock for the singleton resource group.
     * @return An object that can be used to control lock scope.
     * @throws java.lang.IllegalArgumentException This class is not instantiated with {@link #ThreadSafeObject()} constructor.
     * @see ThreadSafeObject.SingleResourceGroup
     */
    protected final LockScope beginWrite() {
        return beginWrite(SingleResourceGroup.INSTANCE);
    }

    /**
     * Acquires write lock for the specified resource group.
     *
     * @param resourceGroup The identifier of the resource group to lock.
     * @return An object that can be used to control lock scope.
     * @throws java.lang.InterruptedException     The waiting thread will be interrupted by another thread.
     * @throws java.lang.IllegalArgumentException The specified resource group is not defined in this object.
     */
    protected final LockScope beginWriteInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        final LockScope scope = getWriteLock(resourceGroup);
        scope.lockInterruptibly();
        return scope;
    }

    /**
     * Acquires write lock for the singleton resource group.
     * @return An object that can be used to control lock scope.
     * @throws java.lang.InterruptedException     The waiting thread will be interrupted by another thread.
     * @throws java.lang.IllegalArgumentException This class is not instantiated with {@link #ThreadSafeObject()} constructor.
     */
    protected final LockScope beginWriteInterruptibly() throws InterruptedException {
        return beginWriteInterruptibly(SingleResourceGroup.INSTANCE);
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

    protected final LockScope beginRead(final Enum<?> resourceGroup) {
        final LockScope scope = getReadLock(resourceGroup);
        scope.lock();
        return scope;
    }

    protected final boolean tryBeginRead(final Enum<?> resourceGroup, final TimeSpan timeout) throws InterruptedException {
        return getReadLock(resourceGroup).tryLock(timeout.duration, timeout.unit);
    }

    protected final boolean tryBeginRead(final TimeSpan timeout) throws InterruptedException {
        return tryBeginRead(SingleResourceGroup.INSTANCE, timeout);
    }

    protected final LockScope beginRead() {
        return beginRead(SingleResourceGroup.INSTANCE);
    }

    protected final LockScope beginReadInterruptibly(final Enum<?> resourceGroup) throws InterruptedException {
        final LockScope scope = getReadLock(resourceGroup);
        scope.lockInterruptibly();
        return scope;
    }

    protected final LockScope beginReadInterruptibly() throws InterruptedException {
        return beginReadInterruptibly(SingleResourceGroup.INSTANCE);
    }

    protected final void endRead(final Enum<?> resourceGroup) {
        getReadLock(resourceGroup).unlock();
    }

    protected final void endRead() {
        endRead(SingleResourceGroup.INSTANCE);
    }

}