package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.AttributeAccessor;

import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class JmxAttributeAccessor extends AttributeAccessor {

    JmxAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    public abstract OpenMBeanAttributeInfo cloneMetadata();

    @Override
    protected abstract Object interceptSet(final Object value) throws InvalidAttributeValueException, InterceptionException;

    @Override
    protected abstract Object interceptGet(final Object value) throws InterceptionException;
}
