package com.bytex.snamp.moa.services;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AttributeWatcher extends AttributeAccessor {

    AttributeWatcher(final MBeanAttributeInfo metadata) {
        super(metadata);
    }
}
