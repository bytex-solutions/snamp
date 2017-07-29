package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.*;

/**
 * Provides access to the attribute through its alias in another connector.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class AliasAttribute extends AbstractCompositeAttribute implements CompositeFeature {
    private static final long serialVersionUID = 8395290268605454780L;
    private final String connectorType;

    AliasAttribute(final String connectorType,
                   final MBeanAttributeInfo info) {
        super(info.getName(), info.getType(), info.getDescription(), info.isReadable(), info.isWritable(), info.isIs(), info.getDescriptor());
        this.connectorType = connectorType;
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }


    Object getValue(final AttributeSupportProvider provider) throws Exception {
        return getValue(provider.getAttributeSupport(connectorType)
                .orElseThrow(() -> attributeNotFound(connectorType, getName())));
    }

    void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        setValue(provider.getAttributeSupport(connectorType).orElseThrow(() -> attributeNotFound(connectorType, getName())), value);
    }

    private Object getValue(final AttributeSupport support) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return support.getAttribute(getName());
    }

    private void setValue(final AttributeSupport support, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        support.setAttribute(new Attribute(getName(), value));
    }
}
