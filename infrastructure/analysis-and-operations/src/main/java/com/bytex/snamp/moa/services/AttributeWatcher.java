package com.bytex.snamp.moa.services;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.gateway.modeling.AttributeValue;

import javax.management.*;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AttributeWatcher extends AttributeAccessor {

    AttributeWatcher(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    void readAttribute(final Collection<AttributeValue> attributes) throws MBeanException, AttributeNotFoundException, ReflectionException {
        attributes.add(getRawValue());
    }
}
