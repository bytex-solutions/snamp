package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.DistributedAttributeRepository;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.NameResolver;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.*;
import javax.management.openmbean.SimpleType;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AttributeComposition extends DistributedAttributeRepository<AbstractCompositeAttribute> implements NameResolver, NotificationListener {
    private final AttributeSupportProvider attributeSupportProvider;
    private final Logger logger;
    private final ExecutorService threadPool;
    private final ScriptLoader scriptLoader;

    AttributeComposition(final String resourceName,
                         final AttributeSupportProvider provider,
                         final ExecutorService threadPool,
                         final Duration syncPeriod,
                         final ScriptLoader loader,
                         final Logger logger){
        super(resourceName, AbstractCompositeAttribute.class, false, syncPeriod);
        attributeSupportProvider = Objects.requireNonNull(provider);
        this.logger = Objects.requireNonNull(logger);
        this.threadPool = Objects.requireNonNull(threadPool);
        this.scriptLoader = Objects.requireNonNull(loader);
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

    private Object resolveAs(final String operand, final WellKnownType expectedType) throws Exception{
        return expectedType.convert(getAttribute(operand));
    }

    /**
     * Takes snapshot of the attribute to distribute it across cluster.
     *
     * @param attribute The attribute that should be synchronized across cluster.
     * @return Serializable state of the attribute; or {@literal null}, if attribute doesn't support synchronization across cluster.
     */
    @Override
    protected Serializable takeSnapshot(final AbstractCompositeAttribute attribute) {
        return Convert.toType(attribute, DistributedAttribute.class, DistributedAttribute::takeSnapshot);
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
    @SuppressWarnings("unchecked")
    public <T> T resolveAs(final String name, final SimpleType<T> expectedType) throws Exception {
        return Convert.toOpenType(resolveAs(name, WellKnownType.getType(expectedType)), expectedType);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final AbstractCompositeAttribute attributeInfo) {
        if (attributeInfo instanceof CompositeFeature) {
            final AttributeSupport support = attributeSupportProvider.getAttributeSupport(((CompositeFeature) attributeInfo).getConnectorType());
            if (support != null)
                support.removeAttribute(attributeInfo.getName());
        }
    }

    @Override
    protected AbstractCompositeAttribute connectAttribute(final String attributeName,
                                                          final AttributeDescriptor descriptor) throws Exception {
        if (CompositeResourceConfigurationDescriptor.isRateFormula(descriptor)) //rate attribute
            return new NotificationRateAttribute(attributeName, descriptor);
        else if(CompositeResourceConfigurationDescriptor.isGroovyFormula(descriptor))   //groovy attribute
            return new GroovyAttribute(attributeName, scriptLoader, descriptor);
        //aggregation
        final AggregationFunction<?> function = CompositeResourceConfigurationDescriptor.parseFormula(descriptor);
        if (function != null)
            return new AggregationAttribute(attributeName, function, this, descriptor);
        //regular attribute
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(connectorType);
        if (support == null)
            throw new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support attributes", connectorType)));
        else {
            //process regular attribute
            final MBeanAttributeInfo underlyingAttribute = support.addAttribute(attributeName, descriptor);
            if (underlyingAttribute == null)
                throw AliasAttribute.attributeNotFound(connectorType, attributeName);
            //check whether the type of function is compatible with type of attribute
            return new AliasAttribute(connectorType, underlyingAttribute);
        }
    }


    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        parallelForEach(attribute -> {
            if (attribute instanceof NotificationListener)
                ((NotificationListener) attribute).handleNotification(notification, handback);
        }, getThreadPool());
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {
        failedToConnectAttribute(logger, Level.WARNING, attributeName, e);
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
    protected void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToGetAttribute(logger, Level.WARNING, attributeID, e);
    }

    @Override
    protected void setAttribute(final AbstractCompositeAttribute attribute, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        if(attribute instanceof AliasAttribute)
            ((AliasAttribute) attribute).setValue(attributeSupportProvider, value);
        else
            throw new UnsupportedOperationException();
    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
    }
}
