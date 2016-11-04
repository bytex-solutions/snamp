package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.notifications.measurement.NotificationSource;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a channel used to submit parsed notifications.
 * @since 2.0
 * @version 2.0
 */
public final class NotificationDispatcher extends NotificationSource implements NotificationListener {
    private static final long serialVersionUID = -2408380605308866380L;
    transient final MessageDrivenAttributeRepository attributes;
    transient final MessageDrivenNotificationRepository notifications;
    transient private final NotificationParser notificationParser;
    transient private final Logger logger;

    NotificationDispatcher(final String componentName,
                           final String instanceName,
                           final MessageDrivenAttributeRepository attributes,
                           final MessageDrivenNotificationRepository notifications,
                           final Logger logger,
                           final NotificationParser parser){
        super(componentName, instanceName);
        this.attributes = Objects.requireNonNull(attributes);
        this.notifications = Objects.requireNonNull(notifications);
        this.notificationParser = Objects.requireNonNull(parser);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSource(this);
        attributes.handleNotification(notification, handback);
        notifications.handleNotification(notification, handback);
    }

    public boolean handleNotification(final Map<String, ?> headers, final Object body, final Object context) {
        final Notification n;
        try {
            n = notificationParser.parse(headers, body);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, String.format("Failed to parse notification '%s' with headers '%s'", body, headers), e);
            return false;
        }
        final boolean success;
        if (success = n != null)
            handleNotification(n, context);
        else
            logger.warning(String.format("Notification '%s' with headers '%s' is ignored by parser", body, headers));
        return success;
    }
}
