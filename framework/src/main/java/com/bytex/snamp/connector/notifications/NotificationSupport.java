package com.bytex.snamp.connector.notifications;


import com.bytex.snamp.configuration.EventConfiguration;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcaster;
import java.util.Collection;
import java.util.Set;

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
     * Enables managed resource notification.
     * <p>
     *     When implementing this method you must take into
     *     account already existed notifications in the managed resource connector.
     *     If notification is enabled in the managed resource connector then
     *     it should re-enable the notification (disable and then enable again).
     * @param category The notification category.
     * @param descriptor The notification configuration options.
     * @return Metadata of created notification.
     * @since 2.0
     */
    MBeanNotificationInfo enableNotifications(final String category, final NotificationDescriptor descriptor);

    /**
     * Disables notifications of the specified category.
     * @param category Category of notifications to disable.
     * @return An instance of disabled notification category; or {@literal null}, if notification with the specified category doesn't exist.
     * @since 2.0
     */
    MBeanNotificationInfo disableNotifications(final String category);

    /**
     * Disables all notifications except specified in the collection.
     * @param events A set of subscription lists which should not be disabled.
     * @since 2.0
     */
    void retainNotifications(final Set<String> events);

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
     * Determines whether this repository can be populated with notifications using call of {@link #expandNotifications()}.
     * @return {@literal true}, if this repository can be populated; otherwise, {@literal false}.
     * @since 2.0
     */
    boolean canExpandNotifications();

    /**
     * Populate this repository with notifications.
     *
     * @return A collection of registered notifications; or empty collection if nothing to populate.
     * @since 2.0
     */
    Collection<? extends MBeanNotificationInfo> expandNotifications();

    /**
     * Suspends or activate raising of events.
     * @param value {@literal true} to suspend events; {@literal false}, to activate events.
     */
    void setSuspended(final boolean value);
}
