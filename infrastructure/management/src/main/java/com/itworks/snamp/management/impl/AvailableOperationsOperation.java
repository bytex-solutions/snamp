package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableList;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AvailableOperationsOperation extends AvailableFeaturesOperation<MBeanOperationInfo> {
    static final String NAME = "getAvailableOperations";

    AvailableOperationsOperation() {
        super(NAME);
    }

    @Override
    protected ImmutableList<MBeanOperationInfo> extractFeatures(final MBeanInfo metadata) {
        return ImmutableList.copyOf(metadata.getOperations());
    }
}
