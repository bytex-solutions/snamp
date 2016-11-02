package com.bytex.snamp.connector.md;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Objects;

/**
 * Represents a channel used to submit parsed notifications.
 * @since 2.0
 * @version 2.0
 */
public final class NotificationChannel implements NotificationListener {
    final MessageDrivenAttributeRepository attributes;
    private final NotificationParser notificationParser;

    NotificationChannel(final MessageDrivenAttributeRepository attributes,
                        final NotificationParser parser){
        this.attributes = Objects.requireNonNull(attributes);
        this.notificationParser = Objects.requireNonNull(parser);
    }


    @Override
    public void handleNotification(final Notification notification, final Object handback) {

    }
}
