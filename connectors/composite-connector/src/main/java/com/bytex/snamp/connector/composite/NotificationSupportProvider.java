package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.notifications.NotificationSupport;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface NotificationSupportProvider {
    NotificationSupport getNotificationSupport(final String connectorType);
}
