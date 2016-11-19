package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.gateway.modeling.NotificationAccessor;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class NotificationPoint extends NotificationAccessor {
    /**
     * Initializes a new managed resource notification accessor.
     *
     * @param metadata The metadata of the managed resource notification. Cannot be {@literal null}.
     */
    NotificationPoint(final MBeanNotificationInfo metadata) {
        super(metadata);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if(notification instanceof AttributeChangeNotification){

        }
    }
}
