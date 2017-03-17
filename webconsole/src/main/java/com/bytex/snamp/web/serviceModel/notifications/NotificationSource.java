package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.notifications.NotificationSupport;

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

    String getResourceName(){
        return resourceName;
    }

    MBeanNotificationInfo getNotificationMetadata(final Notification notification){
        return notificationSupport.getNotificationInfo(notification.getType());
    }
}
