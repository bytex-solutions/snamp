package com.bytex.snamp.connector.attributes.reflection;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenType;

/**
 * Represents attribute marshaller for custom attribute types.
 * @param <T> JMX-compliant type.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public interface ManagementAttributeMarshaller<T>{
    /**
     * Gets JMX-compliant type of the attribute.
     * @return JMX-compliant type of the attribute.
     */
    OpenType<T> getOpenType();

    /**
     * Converts attribute value to the JMX-compliant type.
     * @param attributeValue The value of the attribute.
     * @param metadata The metadata of the bean property.
     * @return JMX-compliant attribute value.
     */
    T toJmxValue(final Object attributeValue, final MBeanAttributeInfo metadata);

    /**
     * Converts JMX-compliant attribute value into the native Java object.
     * @param jmxValue The value to convert.
     * @param metadata The metadata of the bean property.
     * @return The converted attribute value.
     */
    Object fromJmxValue(final T jmxValue, final MBeanAttributeInfo metadata);
}
