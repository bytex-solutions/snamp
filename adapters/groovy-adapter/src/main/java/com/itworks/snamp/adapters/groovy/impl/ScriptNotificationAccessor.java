package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.NotificationRouter;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptNotificationAccessor extends NotificationRouter {
    private final String resourceName;

    ScriptNotificationAccessor(final String resourceName,
                               final MBeanNotificationInfo metadata,
                               final NotificationListener destination) {
        super(metadata, destination);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }
}
