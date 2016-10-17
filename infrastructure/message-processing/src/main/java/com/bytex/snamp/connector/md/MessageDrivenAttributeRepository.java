package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.core.DistributedServices;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MessageDrivenAttributeRepository extends DistributedAttributeRepository<MessageDrivenAttribute> {

    private final ConcurrentMap<String, Object> storage;

    MessageDrivenAttributeRepository(final String resourceName,
                                     final ExecutorService threadPool) {
        super(resourceName, MessageDrivenAttribute.class, false, threadPool);
        this.storage = DistributedServices.getDistributedStorage(getBundleContextOfObject(this), resourceName.concat("-attributes"));
    }

    @Override
    protected Optional<AttributeSnapshot> takeSnapshot(final MessageDrivenAttribute attribute) {
        if(attribute instanceof MetricHolderAttribute<?>)
            return ((MetricHolderAttribute<?>)attribute).getMetric()
    }

    @Override
    protected boolean applySnapshot(final MessageDrivenAttribute attribute, final AttributeSnapshot snapshot) {
        return false;
    }

    @Override
    protected MessageDrivenAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        return null;
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {

    }

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param metadata The metadata of the attribute.
     * @return The value of the attribute retrieved.
     * @throws Exception Internal connector error.
     */
    @Override
    protected Object getAttribute(final MessageDrivenAttribute metadata) throws Exception {
        return metadata.getValue(storage);
    }

    @Override
    protected void failedToGetAttribute(final String attributeID, final Exception e) {

    }

    @Override
    protected void setAttribute(final MessageDrivenAttribute attribute, final Object value) throws Exception {

    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {

    }

    void post(final MeasurementNotification notification) {
        parallelForEach(attribute -> attribute.accept(notification), threadPool);
    }
}
