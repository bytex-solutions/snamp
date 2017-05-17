package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.EvaluationContext;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AttributeComposition extends DistributedAttributeRepository<AbstractCompositeAttribute> implements EvaluationContext, NotificationListener {
    private static final Duration BATCH_READ_WRITE_TIMEOUT = Duration.ofSeconds(30);
    private final AttributeSupportProvider attributeSupportProvider;
    private final ExecutorService threadPool;
    private final ScriptLoader scriptLoader;

    AttributeComposition(final String resourceName,
                         final AttributeSupportProvider provider,
                         final ExecutorService threadPool,
                         final Duration syncPeriod,
                         final ScriptLoader loader){
        super(resourceName, AbstractCompositeAttribute.class, false, syncPeriod);
        attributeSupportProvider = Objects.requireNonNull(provider);
        this.threadPool = Objects.requireNonNull(threadPool);
        this.scriptLoader = Objects.requireNonNull(loader);
    }

    private Object resolveAs(final String operand, final WellKnownType expectedType) throws Exception{
        return expectedType.convert(getAttribute(operand));
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        try {
            return getAttributesParallel(threadPool, attributes, BATCH_READ_WRITE_TIMEOUT);
        } catch (final MBeanException e) {
            getLogger().log(Level.SEVERE, "Unable to read attributes", e.getCause());
            return new AttributeList();
        }
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        try {
            return setAttributesParallel(threadPool, attributes, BATCH_READ_WRITE_TIMEOUT);
        } catch (final MBeanException e) {
            getLogger().log(Level.SEVERE, "Unable to write attributes", e.getCause());
            return new AttributeList();
        }
    }

    @Override
    public AttributeList getAttributes() throws MBeanException {
        return getAttributesParallel(threadPool, BATCH_READ_WRITE_TIMEOUT);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected Optional<? extends Serializable> takeSnapshot(final AbstractCompositeAttribute attribute) {
        return Convert.toType(attribute, DistributedAttribute.class)
                .map(DistributedAttribute::takeSnapshot);
    }

    /**
     * Initializes state of the attribute using its serializable snapshot.
     *
     * @param attribute The attribute to initialize.
     * @param snapshot  Serializable snapshot used for initialization.
     */
    @Override
    protected void loadFromSnapshot(final AbstractCompositeAttribute attribute, final Serializable snapshot) {
        if (attribute instanceof DistributedAttribute)
            ((DistributedAttribute) attribute).loadFromSnapshot(snapshot);
    }

    @Override
    public <T> T resolveName(final String name, final SimpleType<T> expectedType) throws Exception {
        return Convert.toType(resolveAs(name, WellKnownType.getType(expectedType)), expectedType).orElseThrow(OpenDataException::new);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final AbstractCompositeAttribute attributeInfo) {
        if (attributeInfo instanceof CompositeFeature)
            attributeSupportProvider.getAttributeSupport(((CompositeFeature) attributeInfo).getConnectorType())
                    .ifPresent(support -> support.removeAttribute(attributeInfo.getName()));
    }

    @Override
    protected AbstractCompositeAttribute connectAttribute(final String attributeName,
                                                          final AttributeDescriptor descriptor) throws Exception {
        if (CompositeResourceConfigurationDescriptor.isRateFormula(descriptor)) //rate attribute
            return new NotificationRateAttribute(attributeName, descriptor);
        else if (CompositeResourceConfigurationDescriptor.isGroovyFormula(descriptor))   //groovy attribute
            return new GroovyAttribute(attributeName, scriptLoader, descriptor);
        //aggregation
        final AggregationFunction<?> function = CompositeResourceConfigurationDescriptor.parseFormula(descriptor);
        if (function != null)
            return new AggregationAttribute(attributeName, function, this, descriptor);
        //regular attribute
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(connectorType)
                .orElseThrow(() -> new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support attributes", connectorType))));
        //process regular attribute
        final MBeanAttributeInfo underlyingAttribute = support.addAttribute(attributeName, descriptor)
                .orElseThrow(() -> AliasAttribute.attributeNotFound(connectorType, attributeName));
        //check whether the type of function is compatible with type of attribute
        return new AliasAttribute(connectorType, underlyingAttribute);
    }


    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        forEach(attribute -> {
            if (attribute instanceof NotificationListener)
                ((NotificationListener) attribute).handleNotification(notification, handback);
        });
    }

    @Override
    protected Object getAttribute(final AbstractCompositeAttribute metadata) throws Exception {
        if(metadata instanceof AliasAttribute)
            return ((AliasAttribute) metadata).getValue(attributeSupportProvider);
        else if(metadata instanceof ProcessingAttribute)
            return ((ProcessingAttribute) metadata).getValue(this);
        else if(metadata instanceof MetricAttribute<?>)
            return ((MetricAttribute<?>) metadata).getValue();
        else
            throw new UnsupportedOperationException();
    }

    @Override
    protected void setAttribute(final AbstractCompositeAttribute attribute, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        if(attribute instanceof AliasAttribute)
            ((AliasAttribute) attribute).setValue(attributeSupportProvider, value);
        else
            throw new UnsupportedOperationException();
    }
}
