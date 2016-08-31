package com.bytex.snamp.connector.attributes;

import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;

/**
 * Represents attribute of JMX open type that provides read/write methods.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class OpenMBeanAttributeAccessor<T> extends OpenMBeanAttributeInfoImpl implements OpenMBeanAttributeInfo, AttributeDescriptorRead {
    private static final long serialVersionUID = 9200767724267121006L;

    protected OpenMBeanAttributeAccessor(final String attributeID,
                                         final String description,
                                         final OpenType<T> attributeType,
                                         final AttributeSpecifier specifier,
                                         final AttributeDescriptor descriptor){
        super(attributeID,
                attributeType,
                description,
                specifier,
                descriptor);
    }

    /**
     * Gets value of this attribute.
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    protected abstract T getValue() throws Exception;

    /**
     * Sets value of this attribute.
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    protected abstract void setValue(final T value) throws Exception;
}
