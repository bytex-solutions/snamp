package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableList;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AvailableAttributesOperation extends AvailableFeaturesOperation<MBeanAttributeInfo> {
    static final String NAME = "getAvailableAttributes";

    AvailableAttributesOperation() {
        super(NAME);
    }

    @Override
    protected ImmutableList<MBeanAttributeInfo> extractFeatures(final MBeanInfo metadata) {
        return ImmutableList.copyOf(metadata.getAttributes());
    }
}
