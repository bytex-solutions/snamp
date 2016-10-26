package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.composite.functions.FunctionParserException;

import javax.management.*;

/**
 * Provides access to the attribute of the resource participated in composition.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
class CompositeAttribute extends AbstractCompositeAttribute implements CompositeFeature {
    private static final long serialVersionUID = 8395290268605454780L;
    private final String connectorType;

    CompositeAttribute(final String connectorType,
                       final String name,
                       final String type,
                       final String description,
                       final boolean isReadable,
                       final boolean isWritable,
                       final boolean isIs,
                       final Descriptor descriptor) {
        super(name, type, description, isReadable, isWritable, isIs, descriptor);
        this.connectorType = connectorType;
    }

    CompositeAttribute(final String connectorType,
                       final MBeanAttributeInfo info) throws FunctionParserException {
        this(connectorType, info.getName(), info.getType(), info.getDescription(), info.isReadable(), info.isWritable(), info.isIs(), info.getDescriptor());
    }

    @Override
    public final String getConnectorType() {
        return connectorType;
    }

    final Object getValue(final AttributeSupportProvider provider) throws Exception {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if(support == null)
            throw attributeNotFound(connectorType, getName());
        else
            return getValue(support);
    }

    final void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if (support == null)
            throw attributeNotFound(connectorType, getName());
        else
            setValue(support, value);
    }

    Object getValue(final AttributeSupport support) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return support.getAttribute(getName());
    }

    void setValue(final AttributeSupport support, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        support.setAttribute(new Attribute(getName(), value));
    }
}
