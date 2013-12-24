package com.snamp.connectors;

import java.io.Serializable;
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
     * Represents notification.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface Notification extends Map<String, Object> {
        /**
         * Represents severity of the event.
         * @author Roman Sakno
         * @version 1.0
         * @since 1.0
         */
        public static enum Severity implements Serializable {
            /**
             * Severity is unknown.
             */
            UNKNOWN,

            /**
             * A "panic" condition usually affecting multiple apps/servers/sites.
             * At this level it would usually notify all tech staff on call.
             */
            PANIC,

            /**
             * Should be corrected immediately, therefore notify staff who can fix the problem.
             * An example would be the loss of a primary ISP connection.
             */
            ALERT,

            /**
             * Should be corrected immediately, but indicates failure in a secondary system,
             * an example is a loss of a backup ISP connection.
             */
            CRITICAL,

            /**
             * Non-urgent failures, these should be relayed to developers or admins;
             * each item must be resolved within a given time.
             */
            ERROR,

            /**
             * Warning messages, not an error, but indication that an error will occur if action is not taken,
             * e.g. file system 85% full - each item must be resolved within a given time.
             */
            WARNING,

            /**
             * Events that are unusual but not error conditions - might be summarized in an email to
             * developers or admins to spot potential problems - no immediate action required.
             */
            NOTICE,

            /**
             * Normal operational messages - may be harvested for reporting,
             * measuring throughput, etc. - no action required.
             */
            INFO,

            /**
             * Info useful to developers for debugging the application, not useful during operations.
             */
            DEBUG
        }

        /**
         * Represents name of the notification property that may contains
         * facility level of the event.
         * <p>
         *     For more information, see SYSLOG facility levels.
         * </p>
         */
        static final String FACILITY_LEVEL = "facilityLevel";

        /**
         * Gets the date and time at which the notification is generated.
         * @return The date and time at which the notification is generated.
         */
        public Date getTimeStamp();

        /**
         * Gets the order number of the notification message.
         * @return The order number of the notification message.
         */
        public long getSequenceNumber();

        /**
         * Gets a severity of this event.
         * @return The severity of this event.
         */
        public Severity getSeverity();

        /**
         * Gets a message description of this notification.
         * @return The message description of this notification.
         */
        public String getMessage();

        /**
         * Gets attachments associated with this notification.
         * <p>
         *     The key of the returned map contains name of the attachment.
         * </p>
         * @return A invoke-only collection of attachments associated with this notification.
         */
        public Map<String, Object> getAttachments();
    }

    /**
     * Represents notification listener.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface NotificationListener extends EventListener {

        /**
         * Handles the specified notification.
         * @param n The notification to handle.
         * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
         */
        public boolean handle(final Notification n);
    }

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
    public NotificationMetadata enableNotifications(final String listId, final String category, final Map<String, String> options);

    /**
     * Disables event listening for the specified category of events.
     * <p>
     *     This method removes all listeners associated with the specified subscription list.
     * </p>
     * @param listId The identifier of the subscription list.
     * @return {@literal true}, if notifications for the specified category is previously enabled; otherwise, {@literal false}.
     */
    public boolean disableNotifications(final String listId);

    /**
     * Gets the notification metadata by its category.
     * @param listId The identifier of the subscription list.
     * @return The metadata of the specified notification category; or {@literal null}, if notifications
     * for the specified category is not enabled by {@link #enableNotifications(String, String, java.util.Map)} method.
     */
    public NotificationMetadata getNotificationInfo(final String listId);

    /**
     * Attaches the notification listener.
     * @param listId The identifier of the subscription list.
     * @param listener The notification listener.
     * @return An identifier of the notification listener generated by this connector.
     */
    public Object subscribe(final String listId, final NotificationListener listener);

    /**
     * Removes the notification listener.
     * @param listenerId An identifier previously returned by {@link #subscribe(String, NotificationListener)}.
     * @return {@literal true} if listener is removed successfully; otherwise, {@literal false}.
     */
    public boolean unsubscribe(final Object listenerId);
}
