package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.measurement.NotificationSource;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a channel used to submit parsed notifications.
 * @since 2.0
 * @version 2.0
 */
public final class NotificationChannel extends NotificationSource implements NotificationListener {
    private static final long serialVersionUID = -2408380605308866380L;
    transient final MessageDrivenAttributeRepository attributes;
    transient final MessageDrivenNotificationRepository notifications;
    transient private final NotificationParser notificationParser;

    NotificationChannel(final String componentName,
                        final String instanceName,
                        final MessageDrivenAttributeRepository attributes,
                        final MessageDrivenNotificationRepository notifications,
                        final NotificationParser parser){
        super(componentName, instanceName);
        this.attributes = Objects.requireNonNull(attributes);
        this.notifications = Objects.requireNonNull(notifications);
        this.notificationParser = Objects.requireNonNull(parser);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSource(this);
        attributes.handleNotification(notification, handback);
        notifications.handleNotification(notification, handback);
    }

    public void handleNotification(final Map<String, ?> headers, final Object body, final Object context) throws Exception {
        final Notification n = notificationParser.parse(headers, body);
        handleNotification(n, context);
    }
}
