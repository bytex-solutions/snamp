package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.Box;
import com.bytex.snamp.concurrent.Repeater;
import org.osgi.framework.BundleContext;

import javax.management.MBeanAttributeInfo;
import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.core.DistributedServices.getDistributedStorage;
import static com.bytex.snamp.core.DistributedServices.isActiveNode;
import static com.bytex.snamp.internal.Utils.*;

/**
 * Represents repository for attributes which state can be synchronized across cluster nodes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class DistributedAttributeRepository<M extends MBeanAttributeInfo> extends AbstractAttributeRepository<M> {
    private static final String STORAGE_NAME_POSTFIX = "-attributes";
    private static final double ACTIVE_SYNC_TIME_FACTOR = Math.sqrt(2);
    private static final int SYNC_THREAD_PRIORITY = Thread.MIN_PRIORITY;

    /**
     * Factory used to instantiate internal state storage of the specified type.
     */
    protected static final class StateStorageFactory{
        private final String storageKey;
        private final ConcurrentMap<String, Object> storage;

        private StateStorageFactory(final ConcurrentMap<String, Object> storage, final String storageKey){
            this.storageKey = storageKey;
            this.storage = storage;
        }

        public <T extends Serializable> Box<T> ofType(final Class<T> storageType){
            return new AttributeStateStorage<>(storage, storageKey, storageType);
        }
    }

    private final class SynchronizationJob extends Repeater{
        private final ConcurrentMap<String, Object> storage;

        private SynchronizationJob(final Duration syncPeriod) {
            super(syncPeriod);
            storage = getDistributedStorage(getBundleContext(), getResourceName().concat(STORAGE_NAME_POSTFIX));
        }

        @Override
        protected int getPriority() {
            return SYNC_THREAD_PRIORITY;
        }

        @Override
        protected String generateThreadName() {
            return "SyncThread-".concat(getResourceName());
        }

        private BundleContext getBundleContext() {
            return getBundleContextOfObject(DistributedAttributeRepository.this);
        }

        @Override
        public Duration getPeriod() {
            Duration period = super.getPeriod();
            //synchronization period depends on cluster node type. Active node should be synchronized earlier
            if (isActiveNode(getBundleContext())) {
                final double newPeriodMillis = period.toMillis() / ACTIVE_SYNC_TIME_FACTOR;
                period = Duration.ofSeconds(Math.round(newPeriodMillis));
            }
            return period;
        }

        private void sync(final String storageKey, final M attribute) {
            if (isActiveNode(getBundleContext())) {   //save snapshot of the active node into cluster-wide storage
                final Serializable snapshot = takeSnapshot(attribute);
                if (snapshot != null)
                    storage.put(storageKey, snapshot);
            } else {    //passive node should reload its state from the storage
                final Object snapshot = storage.get(storageKey);
                if (snapshot instanceof Serializable)
                    loadFromSnapshot(attribute, (Serializable) snapshot);
            }
        }

        private void sync(final M attribute) {
            getStorageKey(attribute).ifPresent(storageKey -> sync(storageKey, attribute));
        }

        @Override
        protected void doAction() {
            parallelForEach(this::sync, getThreadPool());
        }
    }

    private final SynchronizationJob syncThread;

    protected DistributedAttributeRepository(final String resourceName,
                                             final Class<M> attributeMetadataType,
                                             final boolean expandable,
                                             final Duration syncPeriod) {
        super(resourceName, attributeMetadataType, expandable);
        syncThread = new SynchronizationJob(syncPeriod);
        syncThread.run();
    }

    /**
     * Gets thread pool used to synchronize attribute states across cluster.
     * @return Thread pool instance.
     */
    protected abstract ExecutorService getThreadPool();

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    protected abstract Serializable takeSnapshot(final M attribute);

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
     * @implNote In the default implementation this method always returns storage key using method {@link AttributeDescriptor#getName(String)}.
     */
    protected Optional<String> getStorageKey(final M attribute){
        return Optional.of(AttributeDescriptor.getName(attribute));
    }

    @Override
    public void close() {
        super.close();
        callUnchecked(() -> {
            syncThread.close();
            return null;
        });
    }
}
