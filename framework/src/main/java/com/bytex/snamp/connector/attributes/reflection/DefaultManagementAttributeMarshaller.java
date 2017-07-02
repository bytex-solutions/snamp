package com.bytex.snamp.connector.attributes.reflection;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DefaultManagementAttributeMarshaller implements ManagementAttributeMarshaller<Object> {
    @Override
    public OpenType<Object> getOpenType() {
        return null;
    }

    @Override
    public Object toJmxValue(final Object attributeValue,
                             final MBeanAttributeInfo descriptor) {
        return attributeValue;
    }

    @Override
    public Object fromJmxValue(final Object jmxValue,
                               final MBeanAttributeInfo descriptor) {
        return jmxValue;
    }
}
