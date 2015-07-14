package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableList;

import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AvailableEventsOperation extends AvailableFeaturesOperation<MBeanNotificationInfo> {
    static final String NAME = "getAvailableEvents";

    AvailableEventsOperation() {
        super(NAME);
    }

    @Override
    protected ImmutableList<MBeanNotificationInfo> extractFeatures(final MBeanInfo metadata) {
        return ImmutableList.copyOf(metadata.getNotifications());
    }
}
