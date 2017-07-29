package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.AbstractNotificationInfo;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class SyntheticNotificationInfo extends AbstractNotificationInfo implements NotificationFilter {
    private static final long serialVersionUID = -3224023906663012968L;
    private NotificationFilter filter;

    SyntheticNotificationInfo(final String notifType, final Class<? extends Notification> notificationType, final String description, final NotificationDescriptor descriptor) {
        super(notifType, description, notificationType, descriptor);
        filter = n -> true;
    }

    public SyntheticNotificationInfo(final String notifType, final NotificationDescriptor descriptor) {
        this(notifType, Notification.class, descriptor.getDescription("Simple proxy notification"), descriptor);
    }

    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return filter.isNotificationEnabled(notification);
    }

    final void setupFilter(final DataStreamConnectorConfigurationDescriptionProvider configurationParser){
        filter = configurationParser.parseNotificationFilter(getDescriptor());
    }
}
