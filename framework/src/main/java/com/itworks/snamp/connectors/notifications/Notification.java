package com.itworks.snamp.connectors.notifications;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Represents notification.
 * <p>
 *     Through map interface you can obtain additional notification parameters
 *     called attachments.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface Notification extends Map<String, Object> {
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
    @SuppressWarnings("UnusedDeclaration")
    static final String FACILITY_LEVEL = "facilityLevel";

    /**
     * Gets the date and time at which the notification is generated.
     * @return The date and time at which the notification is generated.
     */
    Date getTimeStamp();

    /**
     * Gets the order number of the notification message.
     * @return The order number of the notification message.
     */
    long getSequenceNumber();

    /**
     * Gets a severity of this event.
     * @return The severity of this event.
     */
    public Severity getSeverity();

    /**
     * Gets a message description of this notification.
     * @return The message description of this notification.
     */
    String getMessage();
}