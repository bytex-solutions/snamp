package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.OpenAttributeRepository;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.core.DistributedServices;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MessageDrivenAttributeRepository extends OpenAttributeRepository<MessageDrivenAttribute> {
    private final ExecutorService threadPool;
    private final ConcurrentMap<String, Object> storage;

    MessageDrivenAttributeRepository(final String resourceName,
                                     final ExecutorService threadPool) {
        super(resourceName, MessageDrivenAttribute.class, false);
        this.threadPool = Objects.requireNonNull(threadPool);
        this.storage = DistributedServices.getDistributedStorage(getBundleContextOfObject(this), resourceName.concat("-attributes"));
    }

    @Override
    protected MessageDrivenAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        return null;
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {

    }

    @Override
    protected void failedToGetAttribute(final String attributeID, final Exception e) {

    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {

    }

    void post(final MeasurementNotification notification) {
        parallelForEach(attribute -> attribute.accept(notification), threadPool);
    }
}
