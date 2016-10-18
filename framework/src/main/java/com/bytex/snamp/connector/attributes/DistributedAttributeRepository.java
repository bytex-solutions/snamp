package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.Box;
import org.osgi.framework.BundleContext;

import javax.management.MBeanAttributeInfo;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static com.bytex.snamp.core.DistributedServices.getDistributedStorage;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents repository for attributes which state can be synchronized across cluster nodes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class DistributedAttributeRepository<M extends MBeanAttributeInfo> extends AbstractAttributeRepository<M> {
    private static final String STORAGE_NAME_POSTFIX = "-attributes";

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

    private final ConcurrentMap<String, Object> storage;

    protected DistributedAttributeRepository(final String resourceName,
                                             final Class<M> attributeMetadataType,
                                             final boolean expandable) {
        super(resourceName, attributeMetadataType, expandable);
        final BundleContext context = getBundleContextOfObject(this);
        storage = getDistributedStorage(context, getResourceName().concat(STORAGE_NAME_POSTFIX));
    }

    /**
     * Gets key used to store the snapshot of the attribute in the cluster-wide storage.
     * @param attributeName The name of the attribute.
     * @param descriptor Cluster-wide attribute. Cannot be {@literal null}.
     * @return Key name used to store the state of the attribute in the cluster-wide storage; or none, if this attribute doesn't support cluster-wide synchronization.
     * @implNote In the default implementation this method always returns storage key using method {@link AttributeDescriptor#getName(String)}.
     */
    protected Optional<String> getStorageKey(final String attributeName, final AttributeDescriptor descriptor){
        return Optional.of(descriptor.getName(attributeName));
    }

    protected abstract M connectAttribute(final String attributeName, final AttributeDescriptor descriptor, final StateStorageFactory storageFactory) throws Exception;

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The name of the attribute.
     * @param descriptor    Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    @Override
    protected final M connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final Optional<String> storageKey = getStorageKey(attributeName, descriptor);
        final StateStorageFactory factory = storageKey.isPresent() ? new StateStorageFactory(storage, storageKey.get()) : null;
        return connectAttribute(attributeName, descriptor, factory);
    }
}
