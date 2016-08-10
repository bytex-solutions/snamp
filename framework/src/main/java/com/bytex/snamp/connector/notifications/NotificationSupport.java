package com.bytex.snamp.connector.notifications;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcaster;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Provides notification support for management connector.
 * <p>
 *     This is an optional infrastructure feature, therefore,
 *     this interface may not be implemented by the management connector.
 * </p>
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface NotificationSupport extends NotificationBroadcaster {
    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the name of the enabled category.
     */
    String NOTIFICATION_CATEGORY_FIELD = "notificationCategory";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the value of the notification severity as {@link com.bytex.snamp.connector.notifications.Severity} value.
     */
    String SEVERITY_FIELD = "severity";


    String DESCRIPTION_FIELD = EventConfiguration.DESCRIPTION_KEY;

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains type descriptor of {@link javax.management.Notification#getUserData()}.
     */
    String USER_DATA_TYPE = "openType";

    /**
     * Gets notification metadata.
     * @param notificationType The type of the notification.
     * @return The notification metadata; or {@literal null}, if notification doesn't exist.
     */
    MBeanNotificationInfo getNotificationInfo(final String notificationType);

    /**
     * Determines whether raising of registered events is suspended.
     * @return {@literal true}, if events are suspended; otherwise {@literal false}.
     */
    boolean isSuspended();

    /**
     * Suspends or activate raising of events.
     * @param value {@literal true} to suspend events; {@literal false}, to activate events.
     */
    void setSuspended(final boolean value);
}
