package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.MapKeyRef;
import org.osgi.framework.BundleContext;

import javax.management.MBeanAttributeInfo;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents repository for attributes which state should be synchronized across cluster nodes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see AttributesDistributionJob
 */
public abstract class DistributedAttributeRepository<M extends MBeanAttributeInfo> extends AbstractAttributeRepository<M> {
    private static final String STORAGE_NAME_POSTIFX = "-attributes";

    private final ConcurrentMap<String, Object> storage;
    private Duration syncPeriod;

    protected DistributedAttributeRepository(final String resourceName,
                                             final Class<M> attributeMetadataType,
                                             final boolean expandable) {
        super(resourceName, attributeMetadataType, expandable);
        final BundleContext context = getBundleContextOfObject(this);
        storage = DistributedServices.getDistributedStorage(context, getResourceName().concat(STORAGE_NAME_POSTIFX));
        syncPeriod = Duration.ZERO;
    }

    public final void setSyncPeriod(final Duration value){
        syncPeriod = Objects.requireNonNull(value);
    }

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
        final Timeout timeout = Duration.ZERO.equals(syncPeriod) ? null : new Timeout(syncPeriod);
        final MapKeyRef<String, Object> dataSlot = new MapKeyRef<>(storage, descriptor.getName(attributeName));

    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final M attributeInfo) {
        super.disconnectAttribute(attributeInfo);
    }
}
