package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.adapters.modeling.AttributeAccessor;

import javax.management.MBeanAttributeInfo;

/**
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ScriptAttributeAccessor extends AttributeAccessor {
    /**
     * Initializes a new attribute accessor.
     *
     * @param metadata The metadata of the attribute. Cannot be {@literal null}.
     */
    ScriptAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }
}
