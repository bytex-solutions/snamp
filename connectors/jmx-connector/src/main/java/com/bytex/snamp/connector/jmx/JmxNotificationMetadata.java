package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.connector.notifications.NotificationDescriptorRead;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
interface JmxNotificationMetadata extends JmxFeatureMetadata, NotificationDescriptorRead {
    String[] getNotifTypes();
}
