package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeType;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class DecomposerAttribute extends UnaryFunctionAttribute {
    private static final long serialVersionUID = -8981662276608894554L;
    private final String fieldName;
    private final CompositeType compositeType;

    @SuppressWarnings("unchecked")
    DecomposerAttribute(final String name,
                        final String sourceAttribute,
                        final String fieldName,
                        final CompositeType compositeType,
                        final AttributeDescriptor descriptor) {
        super(name, sourceAttribute, compositeType.getType(fieldName), compositeType.getDescription(fieldName), descriptor);
        this.fieldName = fieldName;
        this.compositeType = compositeType;
    }

    @Override
    protected Object getValue(final AttributeAccessor operand) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        return operand.getValue(compositeType).get(fieldName);
    }
}
