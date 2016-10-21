package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Represents repository with message-driven attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class MessageDrivenAttributeRepository extends DistributedAttributeRepository<MessageDrivenAttribute> {
    private final ExecutorService threadPool;

    MessageDrivenAttributeRepository(final String resourceName,
                                     final ExecutorService threadPool,
                                     final Duration syncPeriod) {
        super(resourceName, MessageDrivenAttribute.class, false, syncPeriod);
        this.threadPool = Objects.requireNonNull(threadPool);
    }

    @Override
    protected MessageDrivenAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        return null;
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {

    }

    /**
     * Gets thread pool used to synchronize attribute states across cluster.
     *
     * @return Thread pool instance.
     */
    @Override
    protected ExecutorService getThreadPool() {
        return threadPool;
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected Serializable takeSnapshot(final MessageDrivenAttribute attribute) {
        return attribute.takeSnapshot();
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected void loadFromSnapshot(final MessageDrivenAttribute attribute, final Serializable snapshot) {
        attribute.loadFromSnapshot(snapshot);
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
        return metadata.getValue();
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
        parallelForEach(attribute -> attribute.accept(notification), null);
    }
}
