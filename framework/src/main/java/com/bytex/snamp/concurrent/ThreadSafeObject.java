package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SafeCloseable;

import java.util.EnumMap;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Represents an abstract class for all thread-safe objects.
 * <p>
 *     This class provides special methods that helps to synchronize
 *     the access to the fields. The fields may be grouped into the sections
 *     with individual read/write locks.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ThreadSafeObject {

    private interface LockScope extends SafeCloseable, Lock {
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

    private static final class ReentrantReadWriteLockSlim implements ReadWriteLock {
        private final ReadLockScope readLock;
        private final WriteLockScope writeLock;

        private ReentrantReadWriteLockSlim(){
            final ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
            readLock = new ReadLockScope(reentrantLock);
            writeLock = new WriteLockScope(reentrantLock);
        }

        @Override
        public ReadLockScope readLock() {
            return readLock;
        }

        @Override
        public WriteLockScope writeLock() {
            return writeLock;
        }
    }

    private static final class ReadOrWriteLockManager extends LockManager{
        private final Function<Enum<?>, ? extends LockScope> scopeProvider;

        private ReadOrWriteLockManager(final Function<Enum<?>, ? extends LockScope> scopeProvider){
            this.scopeProvider = Objects.requireNonNull(scopeProvider);
        }

        @Override
        <E extends Throwable> SafeCloseable acquireLock(final Enum<?> resourceGroup, final Acceptor<? super Lock, E> acceptor) throws E {
            final LockScope scope = scopeProvider.apply(resourceGroup);
            acceptor.accept(scope);
            return scope;
        }
    }

    /**
     * Represents single resource group
     * @since 1.0
     * @version 2.0
     */
    protected enum SingleResourceGroup {
        INSTANCE
    }

    /**
     * Represents write lock manager.
     */
    protected final LockManager writeLock;
    /**
     * Represents read lock manager.
     */
    protected final LockManager readLock;

    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G>              Enum definition.
     */
    protected <G extends Enum<G>> ThreadSafeObject(final Class<G> resourceGroupDef) {
        final EnumMap<G, ReentrantReadWriteLockSlim> resourceGroups = new EnumMap<>(resourceGroupDef);
        for (final G resourceGroup : resourceGroupDef.getEnumConstants())
            resourceGroups.put(resourceGroup, new ReentrantReadWriteLockSlim());
        writeLock = new ReadOrWriteLockManager(group -> resourceGroups.get(group).writeLock());
        readLock = new ReadOrWriteLockManager(group -> resourceGroups.get(group).readLock());
    }
}