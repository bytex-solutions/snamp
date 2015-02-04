package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.views.View;

import javax.management.JMException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcaster;
import javax.management.openmbean.CompositeData;

/**
 * Provides notification support for management connector.
 * <p>
 *     This is an optional infrastructure feature, therefore,
 *     this interface may not be implemented by the management connector.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationSupport extends NotificationBroadcaster, View {
    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the name of the enabled category.
     */
    String NOTIFICATION_CATEGORY_FIELD = "notificationCategory";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the value of the notification severity as {@link com.itworks.snamp.connectors.notifications.Severity} value.
     */
    String NOTIFICATION_SEVERITY_FIELD = "severity";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the value of the notification subscription model as {@link com.itworks.snamp.connectors.notifications.NotificationSubscriptionModel} value.
     */
    String NOTIFICATION_MODEL_FIELD = "notificationModel";

    /**
     * Enables event listening for the specified category of events.
     * <p>
     *     category can be used for enabling notifications for the same category
     *     but with different options.
     * <p>
     *     listId parameter
     *     is used as a value of {@link javax.management.Notification#getType()}.
     * @param listId An identifier of the subscription list.
     * @param category The name of the event category to listen.
     * @param options Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     * @throws javax.management.JMException Internal connector error.
     */
    MBeanNotificationInfo enableNotifications(final String listId,
                                   final String category,
                                   final CompositeData options) throws JMException;

    /**
     * Disables event listening for the specified category of events.
     * <p>
     *     This method removes all listeners associated with the specified subscription list.
     * </p>
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    boolean disableNotifications(final String listId);
}
