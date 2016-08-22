package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Box;
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

    private CompositeAttribute connectAttribute(final String connectorType, final String attributeName, final AttributeDescriptor descriptor) throws Exception{
        final AttributeSupport support = attributeSupportProvider.getAttributeSupport(connectorType);
        if(support == null)
            throw CompositeAttribute.attributeNotFound(connectorType, attributeName);
        final MBeanAttributeInfo underlyingAttribute = support.addAttribute(attributeName, descriptor.getReadWriteTimeout(), null);
        return new CompositeAttribute(connectorType, underlyingAttribute);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The name of the attribute.
     * @param descriptor    Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    @Override
    protected CompositeAttribute connectAttribute(final String attributeName, final AttributeDescriptor descriptor) throws Exception {
        final Box<String> connectorType = new Box<>();
        final Box<String> name = new Box<>();
        return ConnectorTypeSplit.split(attributeName, connectorType, name) ? connectAttribute(connectorType.get(), name.get(), descriptor) : null;
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
