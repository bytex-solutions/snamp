package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.*;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractCompositeAttribute extends MBeanAttributeInfo implements CompositeFeature {
    private static final long serialVersionUID = 4903492396392396532L;
    private final String connectorType;

    AbstractCompositeAttribute(final String connectorType,
                               final String name,
                               final String type,
                               final String description,
                               final boolean isReadable,
                               final boolean isWritable,
                               final boolean isIs,
                               final Descriptor descriptor){
        super(name, type, description, isReadable, isWritable, isIs, descriptor);
        this.connectorType = Objects.requireNonNull(connectorType);
    }

    @Override
    public final String getConnectorType() {
        return connectorType;
    }

    abstract Object getValue(final AttributeSupport support) throws Exception;

    final Object getValue(final AttributeSupportProvider provider) throws Exception {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if(support == null)
            throw attributeNotFound(connectorType, getName());
        else
            return getValue(support);
    }

    abstract void setValue(final AttributeSupport support, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException;

    final void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if (support == null)
            throw attributeNotFound(connectorType, getName());
        else
            setValue(support, value);
    }

    static AttributeNotFoundException attributeNotFound(final String connectorType, final String attributeName) {
        return new AttributeNotFoundException(String.format("Connector of type '%s' is not defined in connection string. Attribute '%s' cannot be resolved", connectorType, attributeName));
    }
}
