package com.itworks.snamp.connectors.notifications;

import java.util.*;

/**
 * Represents default implementation of the SNAMP notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationImpl extends HashMap<String, Object> implements Notification {
    private final long sequenceNumber;
    private final Date timeStamp;
    private final Severity severity;
    private final String message;
    private transient volatile Object userData;

    /**
     * Initializes a new notification.
     * @param sev The notification severity.
     * @param sequenceNumber The notification sequence number.
     * @param timeStamp Timestamp of the notification.
     * @param message Human-readable description of the notification.
     * @param attachments A collection of attachments.
     */
    public NotificationImpl(final Severity sev, final long sequenceNumber, final Date timeStamp, final String message, final Map<String, Object> attachments){
        super(attachments);
        this.sequenceNumber = sequenceNumber;
        this.timeStamp = timeStamp != null ? timeStamp : new Date();
        this.severity = sev != null ? sev : Severity.UNKNOWN;
        this.message = message != null ? message : "";
        userData = null;
    }

    /**
     * Initializes a new notification without attachments.
     * @param sev The notification severity.
     * @param sequenceNumber The notification sequence number.
     * @param timeStamp Timestamp of the notification.
     * @param message Human-readable description of the notification.
     */
    public NotificationImpl(final Severity sev, final long sequenceNumber, final Date timeStamp, final String message){
        this(sev, sequenceNumber, timeStamp, message, Collections.<String, Object>emptyMap());
    }

    /**
     * Initializes a new notification and copy all properties from the specified notification.
     * @param other The notification to copy.
     */
    public NotificationImpl(final Notification other){
        super(other);
        this.sequenceNumber = other.getSequenceNumber();
        this.timeStamp = other.getTimeStamp();
        this.severity = other.getSeverity();
        this.message = other.getMessage();
    }

    /**
     * Gets the date and time at which the notification is generated.
     *
     * @return The date and time at which the notification is generated.
     */
    @Override
    public Date getTimeStamp() {
        return new Date(timeStamp.getTime());
    }

    /**
     * Gets the order number of the notification message.
     *
     * @return The order number of the notification message.
     */
    @Override
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Gets a severity of this event.
     *
     * @return The severity of this event.
     */
    @Override
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Gets a message description of this notification.
     *
     * @return The message description of this notification.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Gets user data associated with this object.
     *
     * @return The user data associated with this object.
     */
    @Override
    public final Object getUserData() {
        return userData;
    }

    /**
     * Sets the user data associated with this object.
     *
     * @param value The user data to be associated with this object.
     */
    @Override
    public final void setUserData(final Object value) {
        userData = value;
    }
}
