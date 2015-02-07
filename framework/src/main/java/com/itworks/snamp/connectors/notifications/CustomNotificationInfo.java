package com.itworks.snamp.connectors.notifications;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * Represents simplified version of {@link javax.management.MBeanNotificationInfo}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CustomNotificationInfo extends MBeanNotificationInfo {
    /**
     * Constructs an <CODE>CustomNotificationInfo</CODE> object.
     *
     * @param notifType  The name of the notification that can be produced by managed resource.
     * @param description A human readable description of the data.
     */
    public CustomNotificationInfo(final String notifType, final String description) {
        super(new String[]{notifType}, Notification.class.getName(), description);
    }

    /**
     * Constructs an <CODE>MBeanNotificationInfo</CODE> object.
     *
     * @param notifType  The name of the notification that can be produced by managed resource.
     * @param description A human readable description of the data.
     * @param descriptor  The descriptor for the notifications.  This may be null
     *                    which is equivalent to an empty descriptor.
     */
    public CustomNotificationInfo(final String notifType,
                                  final String description,
                                  final NotificationDescriptor descriptor) {
        super(new String[]{notifType}, Notification.class.getName(), description, descriptor);
    }
}
