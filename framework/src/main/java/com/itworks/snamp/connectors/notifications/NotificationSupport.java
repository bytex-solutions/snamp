package com.itworks.snamp.connectors.notifications;

import javax.management.NotificationBroadcaster;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

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
public interface NotificationSupport extends NotificationBroadcaster {
    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the name of the enabled category.
     */
    String NOTIFICATION_CATEGORY_FIELD = "notificationCategory";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the value of the notification severity as {@link com.itworks.snamp.connectors.notifications.Severity} value.
     */
    String SEVERITY_FIELD = "severity";

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains the value of the notification subscription model as {@link com.itworks.snamp.connectors.notifications.NotificationSubscriptionModel} value.
     */
    String SUBSCRIPTION_MODEL_FIELD = "subscriptionModel";


    String DESCRIPTION_FIELD = EventConfiguration.DESCRIPTION_KEY;

    /**
     * The name of the field in {@link javax.management.Descriptor}
     * which contains type descriptor of {@link javax.management.Notification#getUserData()}.
     */
    String USER_DATA_TYPE = "openType";

    /**
     * Gets subscription model.
     * @return The subscription model.
     */
    NotificationSubscriptionModel getSubscriptionModel();
}
