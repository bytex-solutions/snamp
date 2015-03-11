package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.jmx.JMExceptionUtils;

import javax.management.*;
import javax.management.openmbean.OpenMBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AttributeSupportStub implements AttributeSupport {
    static final AttributeSupportStub INSTANCE = new AttributeSupportStub();

    private final OpenMBeanAttributeInfo[] attributes;

    private AttributeSupportStub(){
        attributes = new OpenMBeanAttributeInfo[0];
    }

    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException {
        throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    @Override
    public void setAttribute(final String attributeName, final Object value) throws AttributeNotFoundException {
        throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    @Override
    public OpenMBeanAttributeInfo[] getAttributeInfo() {
        return attributes;
    }
}
