package com.bytex.snamp.connector.composite;

import javax.management.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractCompositeAttribute extends MBeanAttributeInfo {
    private static final long serialVersionUID = 4903492396392396532L;

    AbstractCompositeAttribute(final String name,
                               final String type,
                               final String description,
                               final boolean isReadable,
                               final boolean isWritable,
                               final boolean isIs,
                               final Descriptor descriptor){
        super(name, type, description, isReadable, isWritable, isIs, descriptor);
    }

    abstract Object getValue(final AttributeSupportProvider provider) throws Exception;

    abstract void setValue(final AttributeSupportProvider provider, final Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException;

    static AttributeNotFoundException attributeNotFound(final String connectorType, final String attributeName) {
        return new AttributeNotFoundException(String.format("Connector of type '%s' is not defined in connection string. Attribute '%s' cannot be resolved", connectorType, attributeName));
    }
}
