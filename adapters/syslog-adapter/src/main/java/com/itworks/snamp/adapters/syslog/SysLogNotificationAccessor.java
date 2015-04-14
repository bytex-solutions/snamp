package com.itworks.snamp.adapters.syslog;

import com.itworks.snamp.adapters.NotificationRouter;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SysLogNotificationAccessor extends NotificationRouter {
    private final String resourceName;

    SysLogNotificationAccessor(final String resourceName,
                               final MBeanNotificationInfo metadata,
                               final NotificationListener destination) {
        super(metadata, destination);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(new SysLogSourceInfo(resourceName, getMetadata()));
        return notification;
    }
}
