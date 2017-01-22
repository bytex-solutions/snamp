package com.bytex.snamp.connector.dsp;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.core.LoggerProvider;

import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.ReflectionException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents repository with message-driven attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DataStreamDrivenAttributeRepository extends DistributedAttributeRepository<DataStreamDrivenAttribute> {
    private static final Duration BATCH_READ_WRITE_TIMEOUT = Duration.ofSeconds(30);

    private ExecutorService threadPool;
    private DataStreamDrivenConnectorConfigurationDescriptionProvider configurationParser;

    public DataStreamDrivenAttributeRepository(final String resourceName,
                                               final Duration syncPeriod) {
        super(resourceName, DataStreamDrivenAttribute.class, false, syncPeriod);
    }

    final void init(final ExecutorService threadPool, final DataStreamDrivenConnectorConfigurationDescriptionProvider parser) {
        this.threadPool = Objects.requireNonNull(threadPool);
        this.configurationParser = Objects.requireNonNull(parser);
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        try {
            return getAttributesParallel(threadPool, attributes, BATCH_READ_WRITE_TIMEOUT);
        } catch (final InterruptedException | TimeoutException e) {
            getLogger().log(Level.SEVERE, "Unable to read attributes", e);
            return new AttributeList();
        }
    }



    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        try {
            return setAttributesParallel(threadPool, attributes, BATCH_READ_WRITE_TIMEOUT);
        } catch (final InterruptedException | TimeoutException e) {
            getLogger().log(Level.SEVERE, "Unable to write attributes", e);
            return new AttributeList();
        }
    }

    @Override
    public AttributeList getAttributes() throws MBeanException, ReflectionException {
        return getAttributesParallel(threadPool, BATCH_READ_WRITE_TIMEOUT);
    }

    @Override
    protected DataStreamDrivenAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final String gaugeType = configurationParser.parseGaugeType(descriptor);
        final DataStreamDrivenAttributeFactory factory = AttributeParser.parse(gaugeType);
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
        return threadPool;
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected final Serializable takeSnapshot(final DataStreamDrivenAttribute attribute) {
        return Convert.toType(attribute, DistributedAttribute.class, DistributedAttribute::takeSnapshot, attr -> null);
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected final void loadFromSnapshot(final DataStreamDrivenAttribute attribute, final Serializable snapshot) {
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
    protected final Object getAttribute(final DataStreamDrivenAttribute metadata) throws Exception {
        if (metadata instanceof DistributedAttribute<?, ?>)
            return ((DistributedAttribute<?, ?>) metadata).getValue();
        else if (metadata instanceof ProcessingAttribute<?>)
            return ((ProcessingAttribute<?>) metadata).getValue(this);
        else
            throw new UnrecognizedAttributeTypeException(metadata.getClass());
    }

    @Override
    protected final void setAttribute(final DataStreamDrivenAttribute attribute, final Object value) throws Exception {
        throw DataStreamDrivenAttribute.cannotBeModified(attribute);
    }

    public final void handleNotification(final Notification notification, final BiConsumer<? super DataStreamDrivenAttribute, ? super DataStreamDrivenAttribute.NotificationProcessingResult> callback) {
        parallelForEach(attribute -> callback.accept(attribute, attribute.dispatch(notification)), getThreadPool());
    }

    final void resetAllMetrics(){
        parallelForEach(attribute -> {
            if(attribute instanceof MetricHolderAttribute<?, ?>)
                ((MetricHolderAttribute<?, ?>) attribute).reset();
        }, threadPool);
    }

    @Override
    public void close() {
        threadPool = null;
        configurationParser = null;
        super.close();
    }
}
