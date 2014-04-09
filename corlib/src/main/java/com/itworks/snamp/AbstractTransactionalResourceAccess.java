package com.itworks.snamp;

import java.util.concurrent.locks.Lock;
import org.apache.commons.lang3.mutable.*;
import org.apache.commons.collections4.Factory;

/**
 * Represents transactional (2PC) access to the thread-unsafe resource.
 * @param <R> Type of the thread-unsafe resource to hold.
 * @param <S> Type of the snapshot data that is used to restore the resource.
 * @param <C> Type of the changeset data that is used to commit changes into the resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractTransactionalResourceAccess<R, S, C> extends AbstractConcurrentResourceAccess<R> {
    /**
     * Represents a set of data that can be used to commit or undo changes in the resource.
     * This class cannot be inherited.
     * @param <SNAPSHOT> Type of the data that can be used to undo changes.
     * @param <CHANGESET> Type of the data that can be used to commit changes.
     */
    private static final class PreparationData<SNAPSHOT, CHANGESET>{
        /**
         * Represents the snapshot data that can be used to undo changes.
         */
        public final SNAPSHOT snapshot;

        /**
         * Represents the snapshot data that can be used to commit changes.
         */
        public final CHANGESET changeset;

        public PreparationData(final SNAPSHOT snapshot, final CHANGESET changeset){
            this.snapshot = snapshot;
            this.changeset = changeset;
        }
    }
    private final Mutable<PreparationData<S, C>> snapshot;

    /**
     * Initializes a new transactional resource accessor.
     */
    protected AbstractTransactionalResourceAccess(){
        snapshot = new MutableObject<>();
    }

    /**
     * Represents transaction phase processor.
     * @param <R> Type of the resource to process in the transaction phase.
     * @param <D> Type of the additional data associated with transaction phase.
     * @param <V> Type of the resource processing result.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface TransactionPhaseProcessor<R, D, V>{
        /**
         * Invokes the transaction phase.
         * @param resource The resource to process.
         * @param data The additional data associated with this transaction phase.
         * @return The result of resource processing.
         */
        public V invoke(final R resource, final D data);
    }

    /**
     * Processes the first phase of the commit action and saves the changeset in the memory.
     * @param changesetProvider The changeset provider that creates the changeset that will be saved
     *                          into the resource at the commit phase.
     * @param snapshotProvider The snapshot provider that stores the current state of the resource
     *                         that can be used to restore the resource at the rollback phase.
     * @return {@literal true}, if prepare phase is completed successfully; otherwise, {@literal false}.
     */
    public final boolean prepare(final Factory<C> changesetProvider, final ConsistentAction<R, S> snapshotProvider){
        if(snapshotProvider == null) return false;
        else if(changesetProvider == null) return false;
        final Lock wl = writeLock();
        wl.lock();
        try{
            //at the preparation phase snapshot must be empty
            //if not, then the preparation called twice
            if(snapshot.getValue() != null) return false;
            snapshot.setValue(new PreparationData<>(snapshotProvider.invoke(getResource()), changesetProvider.create()));
            return true;
        }
        finally {
            wl.unlock();
        }
    }

    /**
     * Processes the first phase of the commit action and saves the changeset in the memory.
     * @param changesetProvider The changeset provider that creates the changeset that will be saved
     *                          into the resource at the commit phase.
     * @param snapshotProvider The snapshot provider that stores the current state of the resource
     *                         that can be used to restore the resource at the rollback phase.
     * @return {@literal true}, if prepare phase is completed successfully; otherwise, {@literal false}.
     */
    public final boolean prepare(final ConsistentAction<R, C> changesetProvider, final ConsistentAction<R, S> snapshotProvider){
        return prepare(new Factory<C>() {
            @Override
            public final C create() {
                return changesetProvider.invoke(getResource());
            }
        }, snapshotProvider);
    }

    /**
     * Processes the second phase of the commit action and apply the necessary changes to the resource.
     * <p>
     *     You should check if this method returns {@literal false} then call {@link #rollback(AbstractTransactionalResourceAccess.TransactionPhaseProcessor)} immediately.
     * </p>
     * @param committer The action that apply the changeset to the resource.
     * @return {@literal true}, if second phase of the commit action is completed successfully; otherwise, {@literal false}.
     */
    public final boolean commit(final TransactionPhaseProcessor<R, C, Boolean> committer){
        final Lock wl = writeLock();
        wl.lock();
        try{
            if(snapshot.getValue() == null) return false;
            else if(committer.invoke(getResource(), snapshot.getValue().changeset)){
                snapshot.setValue(null);
                return true;
            }
            else return false;
        }
        finally {
            wl.unlock();
        }
    }

    /**
     * Cancels the commit changes to the resource and restore its state.
     * @param restorer The action that restores the initial state of the resource.
     * @return {@literal true}, if rollback operation is completed successfully; otherwise, {@literal false}.
     */
    public final boolean rollback(final TransactionPhaseProcessor<R, S, Boolean> restorer){
        final Lock wl = writeLock();
        wl.lock();
        try{
            if(snapshot.getValue() == null) return false;
            else if(restorer.invoke(getResource(), snapshot.getValue().snapshot)){
                snapshot.setValue(null);
                return true;
            }
            else return false;
        }
        finally {
            wl.unlock();
        }
    }
}
