package com.itworks.snamp.connectors.notifications;

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
public interface NotificationSupport {

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
     */
    NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options);

    /**
     * Disables event listening for the specified category of events.
     * <p>
     *     This method removes all listeners associated with the specified subscription list.
     * </p>
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    boolean disableNotifications(final String listId);

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
     * @return {@literal true}, if listener is added successfully; otherwise, {@literal false}.
     */
    boolean subscribe(final String listenerId, final NotificationListener listener, final boolean delayed);

    /**
     * Removes the notification listener.
     * @param listenerId An identifier previously returned by {@link #subscribe(String, NotificationListener, boolean)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    boolean unsubscribe(final String listenerId);
}
