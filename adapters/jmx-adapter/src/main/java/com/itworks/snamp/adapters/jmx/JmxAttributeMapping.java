package com.itworks.snamp.adapters.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JmxAttributeMapping {
    OpenMBeanAttributeInfoSupport getAttributeInfo();
    String getOriginalName();
    Object getValue() throws MBeanException, ReflectionException, AttributeNotFoundException;
    void setValue(final Object value) throws MBeanException, ReflectionException, AttributeNotFoundException, InvalidAttributeValueException;
}
