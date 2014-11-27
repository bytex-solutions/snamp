package com.itworks.snamp.adapters.jmx;

import javax.management.DynamicMBean;
import javax.management.Notification;
import java.util.Date;

/**
 * Represents wrapped JMX notification.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxNotificationSurrogate extends Notification {
    JmxNotificationSurrogate(final String notificationType,
                                     final String senderResource,
                                     final String message,
                                     final Date timeStamp,
                                     final Object attachment) {
        super(notificationType, senderResource, 0L, message);
        setUserData(attachment);
        setTimeStamp(timeStamp.getTime());
    }

    /**
     * Gets name of the emitter resource.
     *
     * @return The name of the emitter resource.
     */
    @Override
    public String getSource() {
        return (String) super.getSource();
    }

    Notification toWellKnownNotification(final DynamicMBean source, final long sequenceNum) {
        final Notification result = new Notification(getType(), source, sequenceNum, getTimeStamp(), getMessage());
        result.setUserData(getUserData());
        return result;
    }
}
