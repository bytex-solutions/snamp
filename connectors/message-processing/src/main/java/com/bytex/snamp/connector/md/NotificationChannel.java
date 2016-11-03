package com.bytex.snamp.connector.md;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a channel used to submit parsed notifications.
 * @since 2.0
 * @version 2.0
 */
public final class NotificationChannel implements NotificationListener {
    final MessageDrivenAttributeRepository attributes;
    final MessageDrivenNotificationRepository notifications;
    private final NotificationParser notificationParser;

    NotificationChannel(final MessageDrivenAttributeRepository attributes,
                        final MessageDrivenNotificationRepository notifications,
                        final NotificationParser parser){
        this.attributes = Objects.requireNonNull(attributes);
        this.notifications = Objects.requireNonNull(notifications);
        this.notificationParser = Objects.requireNonNull(parser);
    }


    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        attributes.handleNotification(notification, handback);
        notifications.handleNotification(notification, handback);
    }

    public void handleNotification(final Map<String, ?> headers, final Object body, final Object context) throws Exception {
        final Notification n = notificationParser.parse(headers, body);
        handleNotification(n, context);
    }
}
