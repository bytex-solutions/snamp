package com.bytex.snamp.connector.md;

import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.WriteOnceRef;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;

import javax.management.Notification;
import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

/**
 * Represents repository with message-driven attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class MessageDrivenAttributeRepository extends DistributedAttributeRepository<MessageDrivenAttribute> {
    private final WriteOnceRef<ExecutorService> threadPool;
    private final WriteOnceRef<MessageDrivenConnectorConfigurationDescriptionProvider> configurationParser;

    public MessageDrivenAttributeRepository(final String resourceName,
                                     final Duration syncPeriod) {
        super(resourceName, MessageDrivenAttribute.class, false, syncPeriod);
        threadPool = new WriteOnceRef<>();
        configurationParser = new WriteOnceRef<>();
    }

    final void init(final ExecutorService threadPool, final MessageDrivenConnectorConfigurationDescriptionProvider parser) {
        this.threadPool.set(threadPool);
        this.configurationParser.set(parser);
    }


    @Override
    protected MessageDrivenAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final String gaugeType = configurationParser.get().parseGaugeType(descriptor);
        final MessageDrivenAttributeFactory factory = AttributeParser.parse(gaugeType);
        if(factory == null)
            throw new UnrecognizedAttributeTypeException(gaugeType);
        return factory.createAttribute(attributeName, descriptor);
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
        return Convert.toType(attribute, DistributedAttribute.class, DistributedAttribute::takeSnapshot, attr -> null);
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected final void loadFromSnapshot(final MessageDrivenAttribute attribute, final Serializable snapshot) {
        if (attribute instanceof DistributedAttribute<?, ?>)
            ((DistributedAttribute<?, ?>) attribute).loadFromSnapshot(snapshot);
    }

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param metadata The metadata of the attribute.
     * @return The value of the attribute retrieved.
     * @throws Exception Internal connector error.
     */
    @Override
    protected final Object getAttribute(final MessageDrivenAttribute metadata) throws Exception {
        if (metadata instanceof DistributedAttribute<?, ?>)
            return ((DistributedAttribute<?, ?>) metadata).getValue();
        else if (metadata instanceof ProcessingAttribute<?>)
            return ((ProcessingAttribute<?>) metadata).getValue(this);
        else
            throw new UnrecognizedAttributeTypeException(metadata.getClass());
    }

    @Override
    protected final void setAttribute(final MessageDrivenAttribute attribute, final Object value) throws Exception {
        throw MessageDrivenAttribute.cannotBeModified(attribute);
    }

    public final void handleNotification(final Notification notification, final BiConsumer<? super MessageDrivenAttribute, ? super MessageDrivenAttribute.NotificationProcessingResult> callback) {
        parallelForEach(attribute -> callback.accept(attribute, attribute.dispatch(notification)), getThreadPool());
    }

    final void resetAllMetrics(){
        parallelForEach(attribute -> {
            if(attribute instanceof MetricHolderAttribute<?, ?>)
                ((MetricHolderAttribute<?, ?>) attribute).reset();
        }, threadPool.get());
    }
}
