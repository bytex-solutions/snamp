package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AttributeComposition extends AbstractAttributeRepository<CompositeAttribute> {
    private final AttributeSupportProvider attributeSupportProvider;
    private final Logger logger;

    AttributeComposition(final String resourceName,
                         final AttributeSupportProvider provider,
                         final Logger logger){
        super(resourceName, CompositeAttribute.class, false);
        attributeSupportProvider = Objects.requireNonNull(provider);
        this.logger = Objects.requireNonNull(logger);
    }

    /**
     * Removes the attribute from the connector.
     *
     * @param attributeInfo An attribute metadata.
     */
    @Override
    protected void disconnectAttribute(final CompositeAttribute attributeInfo) {
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(attributeInfo.getConnectorType());
        if (support != null)
            support.removeAttribute(attributeInfo.getName());
    }

    @Override
    protected CompositeAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws ReflectionException, AttributeNotFoundException, MBeanException, AbsentCompositeConfigurationParameterException {
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(connectorType);
        if (support == null)
            throw new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support attributes", connectorType)));
        final MBeanAttributeInfo underlyingAttribute = support.addAttribute(attributeName, descriptor);
        if (underlyingAttribute == null)
            throw CompositeAttribute.attributeNotFound(connectorType, attributeName);
        return new CompositeAttribute(connectorType, underlyingAttribute);
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {
        failedToConnectAttribute(logger, Level.WARNING, attributeName, e);
    }

    @Override
    protected Object getAttribute(final CompositeAttribute metadata) throws MBeanException, AttributeNotFoundException, ReflectionException {
        return metadata.getValue(attributeSupportProvider);
    }

    @Override
    protected void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToGetAttribute(logger, Level.WARNING, attributeID, e);
    }

    @Override
    protected void setAttribute(final CompositeAttribute attribute, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        attribute.setValue(attributeSupportProvider, value);
    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(logger, Level.WARNING, attributeID, value, e);
    }
}
