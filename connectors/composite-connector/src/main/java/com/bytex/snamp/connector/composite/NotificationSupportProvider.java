package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.notifications.NotificationSupport;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@FunctionalInterface
interface NotificationSupportProvider {
    Optional<NotificationSupport> getNotificationSupport(final String connectorType);
}
