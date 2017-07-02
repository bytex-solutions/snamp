package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.notifications.AbstractNotificationInfo;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import javax.management.Notification;

/**
 * Represents Groovy-based notification.
 */
final class GroovyEvent extends AbstractNotificationInfo {
    private static final long serialVersionUID = -6413432323063142285L;

    GroovyEvent(final String notifType,
                final Class<? extends Notification> notificationType,
                        final String description,
                        final NotificationDescriptor descriptor) {
        super(notifType, description, notificationType, descriptor);
    }
}
