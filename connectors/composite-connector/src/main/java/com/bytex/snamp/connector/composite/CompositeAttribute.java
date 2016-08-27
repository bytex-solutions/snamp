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
final class CompositeAttribute extends AbstractCompositeAttribute {
    private static final long serialVersionUID = 8395290268605454780L;

    CompositeAttribute(final String connectorType,
                       final MBeanAttributeInfo info) throws FunctionParserException {
        super(connectorType, info.getName(), info.getType(), info.getDescription(), info.isReadable(), info.isWritable(), info.isIs(), info.getDescriptor());
    }

    @Override
    Object getValue(final AttributeSupport support) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return support.getAttribute(getName());
    }

    @Override
    void setValue(final AttributeSupport support, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {
        support.setAttribute(new Attribute(getName(), value));
    }
}
