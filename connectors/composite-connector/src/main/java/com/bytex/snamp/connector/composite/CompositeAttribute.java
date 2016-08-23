package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Box;
import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.*;
import java.util.Objects;

/**
 * Provides access to the attribute of the resource participated in composition.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CompositeAttribute extends MBeanAttributeInfo implements CompositeFeature {
    private static final long serialVersionUID = 8395290268605454780L;
    private final String connectorType;
    private final String shortName;

    CompositeAttribute(final String attributeName, final MBeanAttributeInfo info) {
        super(attributeName, info.getType(), info.getDescription(), info.isReadable(), info.isWritable(), info.isIs(), info.getDescriptor());
        final Box<String> connectorType = new Box<>();
        final Box<String> name = new Box<>();
        if (ConnectorTypeSplit.split(attributeName, connectorType, name)) {
            this.connectorType = connectorType.get();
            this.shortName = name.get();
        } else
            throw invalidName(attributeName);
    }

    static IllegalArgumentException invalidName(final String attributeName){
        return new IllegalArgumentException("Invalid attribute name: " + attributeName);
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    Object getValue(final AttributeSupportProvider provider) throws AttributeNotFoundException, MBeanException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if(support == null)
            throw attributeNotFound(connectorType, shortName);
        else
            return support.getAttribute(shortName);
    }

    void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if (support == null)
            throw attributeNotFound(connectorType, shortName);
        else
            support.setAttribute(new Attribute(shortName, value));
    }

    static AttributeNotFoundException attributeNotFound(final String connectorType, final String attributeName) {
        return new AttributeNotFoundException(String.format("Connector of type '%s' is not defined in connection string. Attribute '%s' cannot be resolved", connectorType, attributeName));
    }
}
