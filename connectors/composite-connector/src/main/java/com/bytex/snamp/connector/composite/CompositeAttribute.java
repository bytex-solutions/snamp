package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.*;

/**
 * Provides access to the attribute of the resource participated in composition.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CompositeAttribute extends AbstractCompositeAttribute implements CompositeFeature {
    private static final long serialVersionUID = 8395290268605454780L;
    private final String connectorType;

    CompositeAttribute(final String connectorType,
                       final MBeanAttributeInfo info) {
        super(info.getName(), info.getType(), info.getDescription(), info.isReadable(), info.isWritable(), info.isIs(), info.getDescriptor());
        this.connectorType = connectorType;
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }


    Object getValue(final AttributeSupportProvider provider) throws Exception {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if(support == null)
            throw attributeNotFound(connectorType, getName());
        else
            return getValue(support);
    }

    void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        final AttributeSupport support = provider.getAttributeSupport(connectorType);
        if (support == null)
            throw attributeNotFound(connectorType, getName());
        else
            setValue(support, value);
    }

    private Object getValue(final AttributeSupport support) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return support.getAttribute(getName());
    }

    private void setValue(final AttributeSupport support, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        support.setAttribute(new Attribute(getName(), value));
    }
}
