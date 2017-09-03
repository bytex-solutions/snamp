package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.KeyValueStorage;

import javax.management.MBeanAttributeInfo;
import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents repository for attributes which state can be synchronized across cluster nodes.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class DistributedAttributeRepository<M extends MBeanAttributeInfo> extends AbstractAttributeRepository<M> {
    private static final String STORAGE_NAME_POSTFIX = "-attributes";
    private static final double ACTIVE_SYNC_TIME_FACTOR = Math.sqrt(2);
    private static final int SYNC_THREAD_PRIORITY = Thread.MIN_PRIORITY;

    private static final class SynchronizationJob<M extends MBeanAttributeInfo> extends WeakRepeater<DistributedAttributeRepository<M>>{
        private final String threadName;
        private final ClusterMember clusterMember;

        private SynchronizationJob(final Duration syncPeriod, final DistributedAttributeRepository<M> repository) {
            super(syncPeriod, repository);
            threadName = "SyncThread-".concat(repository.getResourceName());
            this.clusterMember = repository.clusterMember;
        }

        @Override
        protected int getPriority() {
            return SYNC_THREAD_PRIORITY;
        }

        @Override
        protected String generateThreadName() {
            return threadName;
        }

        @Override
        public Duration getPeriod() {
            Duration period = super.getPeriod();
            //synchronization period depends on cluster node type. Active node should be synchronized earlier
            if (clusterMember.isActive()) {
                final double newPeriodMillis = period.toMillis() / ACTIVE_SYNC_TIME_FACTOR;
                period = Duration.ofSeconds(Math.round(newPeriodMillis));
            }
            return period;
        }

        @Override
        protected void doAction() throws InterruptedException {
            getReferenceOrTerminate().sync();
        }

        Void terminate() throws TimeoutException, InterruptedException {
            close(getPeriod());
            return null;
        }
    }

    private final SynchronizationJob syncThread;
    private final KeyValueStorage storage;
    private final ClusterMember clusterMember;

    protected DistributedAttributeRepository(final String resourceName,
                                             final Class<M> attributeMetadataType,
                                             final Duration syncPeriod) {
        super(resourceName, attributeMetadataType);
        clusterMember = ClusterMember.get(getBundleContextOfObject(this));
        storage = clusterMember
                .getService(KeyValueStorage.nonPersistent(getResourceName().concat(STORAGE_NAME_POSTFIX)))
                .orElseThrow(AssertionError::new);
        syncThread = new SynchronizationJob<>(syncPeriod, this);
        syncThread.run();
        assert storage.isViewSupported(KeyValueStorage.SerializableRecordView.class);
    }

    final void sync(){
        forEach(this::sync);
    }

    private void sync(final String storageKey, final M attribute) {
        if (clusterMember.isActive())    //save snapshot of the active node into cluster-wide storage
            takeSnapshot(attribute)
                    .ifPresent(serializable -> storage.updateOrCreateRecord(storageKey, KeyValueStorage.SerializableRecordView.class, record -> record.setValue(serializable)));
        else     //passive node should reload its state from the storage
            storage.getRecord(storageKey, KeyValueStorage.SerializableRecordView.class).ifPresent(record -> loadFromSnapshot(attribute, record.getValue()));
    }

    private void sync(final M attribute) {
        getStorageKey(attribute).ifPresent(storageKey -> sync(storageKey, attribute));
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final M attributeInfo) {
        getStorageKey(attributeInfo).ifPresent(storage::delete);
        super.disconnectAttribute(attributeInfo);
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    protected abstract Optional<? extends Serializable> takeSnapshot(final M attribute);

    /**
     * Initializes state of the attribute using its serializable snapshot.
     * @param attribute The attribute to initialize.
     * @param snapshot Serializable snapshot used for initialization.
     */
    protected abstract void loadFromSnapshot(final M attribute, final Serializable snapshot);

    /**
     * Gets key used to store the snapshot of the attribute in the cluster-wide storage.
     * @param attribute Cluster-wide attribute. Cannot be {@literal null}.
     * @return Key name used to store the state of the attribute in the cluster-wide storage; or none, if this attribute doesn't support cluster-wide synchronization.
     * @implNote In the default implementation this method always returns storage key using method {@link AttributeDescriptor#getName(MBeanAttributeInfo)}.
     */
    protected Optional<String> getStorageKey(final M attribute){
        return Optional.of(AttributeDescriptor.getName(attribute));
    }

    @Override
    public void close() {
        super.close();
        callUnchecked(syncThread::terminate);
    }
}
