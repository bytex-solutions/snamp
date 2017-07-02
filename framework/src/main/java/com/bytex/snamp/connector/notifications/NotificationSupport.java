package com.bytex.snamp.connector.notifications;


import com.bytex.snamp.Aggregator;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.ManagedResourceAggregatedService;

import javax.annotation.Nonnull;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcaster;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
public interface NotificationSupport extends NotificationBroadcaster, ManagedResourceAggregatedService {

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
    Optional<? extends MBeanNotificationInfo> enableNotifications(final String category, final NotificationDescriptor descriptor);

    /**
     * Disables notifications of the specified category.
     * @param category Category of notifications to disable.
     * @return An instance of disabled notification category; or {@link Optional#empty()}, if notification with the specified category doesn't exist.
     * @since 2.0
     */
    Optional<? extends MBeanNotificationInfo> disableNotifications(final String category);

    /**
     * Disables all notifications except specified in the collection.
     * @param events A set of subscription lists which should not be disabled.
     * @since 2.0
     */
    void retainNotifications(final Set<String> events);

    /**
     * Gets notification metadata.
     * @param notificationType The type of the notification.
     * @return The notification metadata; or {@link Optional#empty()}, if notification doesn't exist.
     */
    Optional<? extends MBeanNotificationInfo> getNotificationInfo(final String notificationType);

    /**
     * Defines source for all outbound notifications.
     * @param source Source for all outbound notifications. Cannot be {@literal null}.
     * @throws IllegalArgumentException Source object doesn't provide {@link NotificationSupport} object.
     * @implSpec Object of type {@link Aggregator} should provide {@link NotificationSupport} object.
     */
    void setSource(@Nonnull final Aggregator source);

    /**
     * Discover notifications.
     *
     * @return A map of discovered notifications that can be enabled using method {@link #enableNotifications(String, NotificationDescriptor)}.
     * @since 2.0
     */
    default Map<String, NotificationDescriptor> discoverNotifications(){
        return Collections.emptyMap();
    }
}
