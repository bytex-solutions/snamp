package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.connector.FeatureModifiedEvent;

import javax.management.MBeanNotificationInfo;

/**
 * Indicates that the notification provided by managed resource was modified.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class NotificationModifiedEvent extends FeatureModifiedEvent<MBeanNotificationInfo> {
    private static final long serialVersionUID = 6598375803289152860L;

    protected NotificationModifiedEvent(final NotificationSupport sender,
                                        final String resourceName,
                                        final MBeanNotificationInfo feature,
                                        final Modifier type) {
        super(sender, resourceName, feature, type);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final NotificationSupport getSource() {
        return (NotificationSupport) super.getSource();
    }

    public static NotificationModifiedEvent notificationAdded(final NotificationSupport sender,
                                                              final String resourceName,
                                                              final MBeanNotificationInfo feature){
        return new NotificationModifiedEvent(sender, resourceName, feature, Modifier.ADDED);
    }

    public static NotificationModifiedEvent notificationRemoving(final NotificationSupport sender,
                                                              final String resourceName,
                                                              final MBeanNotificationInfo feature){
        return new NotificationModifiedEvent(sender, resourceName, feature, Modifier.REMOVING);
    }
}
