package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.gateway.modeling.NotificationAccessor;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SmtpNotificationSender extends NotificationAccessor {
    

    /**
     * Initializes a new managed resource notification accessor.
     *
     * @param metadata The metadata of the managed resource notification. Cannot be {@literal null}.
     */
    SmtpNotificationSender(final MBeanNotificationInfo metadata) {
        super(metadata);
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     * @param handback     An opaque object which helps the listener to associate
     *                     information regarding the MBean emitter. This object is passed to the
     *                     addNotificationListener call and resent, without modification, to the
     */
    @Override
    public void handleNotification(final Notification notification, final Object handback) {

    }
}
