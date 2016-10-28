package com.bytex.snamp.connector.md;

import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents repository with message-driven attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class MessageDrivenAttributeRepository extends DistributedAttributeRepository<MessageDrivenAttribute> {
    private final WriteOnceRef<ExecutorService> threadPool;
    private final WriteOnceRef<Logger> logger;

    protected MessageDrivenAttributeRepository(final String resourceName,
                                     final Duration syncPeriod) {
        super(resourceName, MessageDrivenAttribute.class, false, syncPeriod);
        threadPool = new WriteOnceRef<>();
        logger = new WriteOnceRef<>();
    }

    final void init(final ExecutorService threadPool, final Logger logger) {
        this.threadPool.set(threadPool);
        this.logger.set(logger);
    }

    protected final Logger getLogger(){
        return logger.get();
    }

    @Override
    protected MessageDrivenAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final String attributeType = descriptor.getName(attributeName);
        final MessageDrivenAttributeFactory factory = AttributeParser.parseAttribute(attributeType);
        if(factory == null)
            throw new UnrecognizedAttributeTypeException(attributeType);
        return factory.apply(attributeName, descriptor);
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {
        failedToConnectAttribute(getLogger(), Level.SEVERE, attributeName, e);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final MessageDrivenAttribute attributeInfo) {
        callUnchecked(() -> {
            attributeInfo.close();
            return null;
        });
    }

    /**
     * Gets thread pool used to synchronize attribute states across cluster.
     *
     * @return Thread pool instance.
     */
    @Override
    protected final ExecutorService getThreadPool() {
        return threadPool.get();
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected final Serializable takeSnapshot(final MessageDrivenAttribute attribute) {
        return attribute.takeSnapshot();
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected final void loadFromSnapshot(final MessageDrivenAttribute attribute, final Serializable snapshot) {
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
        return null;
    }

    @Override
    protected void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToConnectAttribute(logger.get(), Level.SEVERE, attributeID, e);
    }

    @Override
    protected void setAttribute(final MessageDrivenAttribute attribute, final Object value) throws Exception {

    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(logger.get(), Level.SEVERE, attributeID, value, e);
    }

    final void post(final MeasurementNotification notification) {
        parallelForEach(attribute -> attribute.accept(notification), null);
    }
}
