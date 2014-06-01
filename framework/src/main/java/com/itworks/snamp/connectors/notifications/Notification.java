package com.itworks.snamp.connectors.notifications;

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
