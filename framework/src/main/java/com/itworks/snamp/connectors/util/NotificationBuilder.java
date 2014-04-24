package com.itworks.snamp.connectors.util;

import java.util.*;

import static com.itworks.snamp.connectors.NotificationSupport.Notification;

/**
 * Represents in-memory notification builder.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationBuilder {
    /**
     * Determines whether the sequence number
     */
    protected final boolean autoIncrementSequence;
    private long sequenceNum;
    private Date timeStamp;
    private final Map<String, Object> attachments;
    private Notification.Severity severity;
    private String message;

    /**
     * Initializes a new notification builder.
     * @param autoIncrementSequence {@literal true} to increment sequence number after each call of {@link #build()} method;
     *                                             otherwise, {@literal false}.
     */
    public NotificationBuilder(final boolean autoIncrementSequence){
        this.autoIncrementSequence = autoIncrementSequence;
        sequenceNum = 0L;
        timeStamp = null;
        attachments = new HashMap<>(10);
        severity = Notification.Severity.UNKNOWN;
        message = "";
    }

    /**
     * Initializes a new notification builder.
     */
    public NotificationBuilder(){
        this(false);
    }

    /**
     * Gets time stamp used that will be used in generated notifications.
     * @return Time stamp used that will be used in generated notifications.
     */
    public final Date getTimeStamp(){
        return timeStamp;
    }

    /**
     * Sets time stamp used for generating notifications.
     * @param value
     */
    public final void setTimeStamp(final Date value){
        timeStamp = value;
    }

    public final void setSequenceNumber(final long value){
        sequenceNum = value;
    }

    public final long getSequenceNumber(){
        return sequenceNum;
    }

    /**
     * Increments the sequence number and returns a new value.
     * @return Increased sequence number.
     */
    protected final long incrementSequenceNumber(){
        return ++sequenceNum;
    }

    /**
     * Adds a new attachment to the notification.
     * @param name The name of the attachment.
     * @param attachment The attachment content.
     */
    public final void addAttachment(final String name, final Object attachment){
        attachments.put(name, attachment);
    }

    public final boolean removeAttachment(final String name){
        return attachments.remove(name) != null;
    }

    public final void setSeverity(final Notification.Severity value){
        severity = value != null ? value : Notification.Severity.UNKNOWN;
    }

    public final Notification.Severity getSeverity(){
        return severity;
    }

    /**
     * Resets the internal state of this builder.
     */
    public void clear(){
        sequenceNum = 0;
        timeStamp = null;
        attachments.clear();
    }

    public final void setMessage(final String value){
        message = value != null ? value : "";
    }

    public final String getMessage(){
        return message;
    }

    /**
     * Constructs a new instance of the notification.
     * @return A new instance of the notification.
     */
    public Notification build(){
        final Notification result = new NotificationImpl(severity, sequenceNum, timeStamp, message, attachments);
        if(autoIncrementSequence) incrementSequenceNumber();
        return result;
    }
}
