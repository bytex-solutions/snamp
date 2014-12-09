package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.views.View;

import java.util.*;

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
public interface NotificationSupport extends View {

    /**
     * Enables event listening for the specified category of events.
     * <p>
     *     categoryId can be used for enabling notifications for the same category
     *     but with different options.
     * </p>
     * @param listId An identifier of the subscription list.
     * @param category The name of the category to listen.
     * @param options Event discovery options.
     * @return The metadata of the event to listen; or {@literal null}, if the specified category is not supported.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options) throws NotificationSupportException;

    /**
     * Disables event listening for the specified category of events.
     * <p>
     *     This method removes all listeners associated with the specified subscription list.
     * </p>
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     */
    boolean disableNotifications(final String listId) throws NotificationSupportException;

    /**
     * Gets the notification metadata by its category.
     * @param listId The identifier of the subscription list.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     * for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
     */
    NotificationMetadata getNotificationInfo(final String listId);

    /**
     * Returns a read-only collection of enabled notifications (subscription list identifiers).
     * @return A read-only collection of enabled notifications (subscription list identifiers).
     */
    Collection<String> getEnabledNotifications();

    /**
     * Attaches the notification listener.
     * @param listenerId Unique identifier of the listener.
     * @param listener The notification listener.
     * @param delayed {@literal true} to force delayed subscription. This flag indicates
     *                               that you can attach a listener even if this object
     *                               has no enabled notifications.
     * @throws com.itworks.snamp.connectors.notifications.UnknownSubscriptionException The listening is not enabled previously (not raised if {@code delayed} is true).
     * @throws com.itworks.snamp.connectors.notifications.NotificationSupportException Internal connector error.
     * @throws java.lang.IllegalArgumentException listenerId is {@literal null} or empty; or listener is {@literal null}.
     */
    void subscribe(final String listenerId, final NotificationListener listener, final boolean delayed) throws UnknownSubscriptionException, NotificationSupportException;

    /**
     * Removes the notification listener.
     * @param listenerId An identifier previously returned by {@link #subscribe(String, NotificationListener, boolean)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    boolean unsubscribe(final String listenerId);
}
