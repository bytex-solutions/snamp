package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.AbstractNotificationInfo;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class SyntheticNotificationInfo extends AbstractNotificationInfo implements NotificationFilter {
    private static final long serialVersionUID = -3224023906663012968L;
    private final NotificationFilter filter;

    protected SyntheticNotificationInfo(final String notifType, final Class<? extends Notification> notificationType, final String description, final NotificationDescriptor descriptor) throws InvalidSyntaxException {
        super(notifType, description, notificationType, descriptor);
        filter = DataStreamConnectorConfigurationDescriptionProvider.parseNotificationFilter(descriptor);
    }

    public SyntheticNotificationInfo(final String notifType, final NotificationDescriptor descriptor) throws InvalidSyntaxException {
        this(notifType, Notification.class, descriptor.getDescription("Simple proxy notification"), descriptor);
    }

    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return filter.isNotificationEnabled(notification);
    }
}
