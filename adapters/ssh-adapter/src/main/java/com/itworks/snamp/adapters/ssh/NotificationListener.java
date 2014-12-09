package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.connectors.notifications.Notification;

/**
 * Represents notification listener.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface NotificationListener {

    boolean handle(final SshNotificationView metadata,
                   final Notification notif);
}
