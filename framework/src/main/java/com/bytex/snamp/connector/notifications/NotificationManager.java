package com.bytex.snamp.connector.notifications;


import javax.management.JMException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Provides notification support for management connector.
 * <p>
 *     This is an optional infrastructure feature, therefore,
 *     this interface may not be implemented by the management connector.
 * </p>
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface NotificationManager {

    /**
     * Enables managed resource notification.
     * @param category The notification category.
     * @param descriptor The notification configuration options.
     * @since 2.0
     */
    void enableNotifications(final String category, final NotificationDescriptor descriptor) throws JMException;

    /**
     * Disables notifications of the specified category.
     * @param category Category of notifications to disable.
     * @return {@literal true}, if notification is disabled successfully
     * @since 2.0
     */
    boolean disableNotifications(final String category);

    /**
     * Disables all notifications except specified in the collection.
     * @param events A set of subscription lists which should not be disabled.
     * @since 2.0
     */
    void retainNotifications(final Set<String> events);

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
