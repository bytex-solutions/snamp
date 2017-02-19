package com.bytex.snamp.connector.dataStream;

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
public class SyntheticAttributeRepository extends DistributedAttributeRepository<SyntheticAttribute> {
    private static final Duration BATCH_READ_WRITE_TIMEOUT = Duration.ofSeconds(30);

    private ExecutorService threadPool;
    private DataStreamConnectorConfigurationDescriptionProvider configurationParser;

    public SyntheticAttributeRepository(final String resourceName,
                                        final Duration syncPeriod) {
        super(resourceName, SyntheticAttribute.class, false, syncPeriod);
    }

    final void init(final ExecutorService threadPool, final DataStreamConnectorConfigurationDescriptionProvider parser) {
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
    protected SyntheticAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final SyntheticAttributeFactory gaugeFactory = configurationParser.parseGaugeType(descriptor);
        if(gaugeFactory == null)
            throw new UnrecognizedAttributeTypeException(attributeName);
        final SyntheticAttribute attribute = gaugeFactory.createAttribute(attributeName, descriptor);
        attribute.setupFilter(configurationParser);
        return attribute;
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected final Serializable takeSnapshot(final SyntheticAttribute attribute) {
        return Convert.toType(attribute, DistributedAttribute.class, DistributedAttribute::takeSnapshot, attr -> null);
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected final void loadFromSnapshot(final SyntheticAttribute attribute, final Serializable snapshot) {
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
    protected final Object getAttribute(final SyntheticAttribute metadata) throws Exception {
        if (metadata instanceof DistributedAttribute<?, ?>)
            return ((DistributedAttribute<?, ?>) metadata).getValue();
        else if (metadata instanceof DerivedAttribute<?>)
            return ((DerivedAttribute<?>) metadata).getValue(this);
        else
            throw new UnrecognizedAttributeTypeException(metadata.getClass());
    }

    @Override
    protected final void setAttribute(final SyntheticAttribute attribute, final Object value) throws Exception {
        throw SyntheticAttribute.cannotBeModified(attribute);
    }

    public final void handleNotification(final Notification notification, final BiConsumer<? super SyntheticAttribute, ? super SyntheticAttribute.NotificationProcessingResult> callback) {
        forEach(attribute -> callback.accept(attribute, attribute.dispatch(notification)));
    }

    final void resetAllMetrics() {
        forEach(attribute -> {
            if (attribute instanceof MetricHolderAttribute<?, ?>)
                ((MetricHolderAttribute<?, ?>) attribute).reset();
        });
    }

    @Override
    public void close() {
        threadPool = null;
        configurationParser = null;
        super.close();
    }
}
