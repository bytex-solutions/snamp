package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.*;
import java.util.Objects;

/**
 * Provides access to the attribute of the resource participated in composition.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CompositeAttribute extends MBeanAttributeInfo implements ConnectorTypeSplit {
    private static final long serialVersionUID = 8395290268605454780L;
    private final String connectorType;

    CompositeAttribute(final String connectorType, final MBeanAttributeInfo info){
        super(info.getName(), info.getType(), info.getDescription(), info.isReadable(), info.isWritable(), info.isIs(), info.getDescriptor());
        this.connectorType = Objects.requireNonNull(connectorType);
    }

    Object getValue(final AttributeSupportProvider provider) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if(support == null)
            throw attributeNotFound(connectorType, getName());
        else
            return support.getAttribute(getName());
    }

    void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if (support == null)
            throw attributeNotFound(connectorType, getName());
        else
            support.setAttribute(new Attribute(getName(), value));
    }

    static AttributeNotFoundException attributeNotFound(final String connectorType, final String attributeName) {
        return new AttributeNotFoundException(String.format("Connector of type '%s' is not defined in connection string. Attribute '%s' cannot be resolved", connectorType, attributeName));
    }
}
