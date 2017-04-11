package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.notifications.Severity;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * Represents information about notification source.
 */
final class NotificationSource {
    private final NotificationSupport notificationSupport;
    private final String resourceName;

    NotificationSource(final NotificationSupport support, final String resourceName) {
        this.resourceName = resourceName;
        this.notificationSupport = support;
    }

    Severity getSeverity(final Notification notification) {
        final MBeanNotificationInfo metadata = notificationSupport.getNotificationInfo(notification.getType());
        return metadata == null ? Severity.UNKNOWN : NotificationDescriptor.getSeverity(metadata);
    }

    String getResourceName() {
        return resourceName;
    }
}
