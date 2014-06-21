package com.itworks.snamp.adapters.rest;

import com.google.gson.Gson;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.Severity;

import java.util.Date;


/**
 * Represents JSON-serializable notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JsonNotification {
    private String message;
    private Date timeStamp;
    private Severity severity;
    private long sequenceNumber;
    private String category;

    public JsonNotification(){
        message = "";
        timeStamp = new Date();
        severity = Severity.UNKNOWN;
        sequenceNumber = 0L;
        category = "";
    }

    public JsonNotification(final Notification n, final String category){
        this.message = n.getMessage();
        this.timeStamp = n.getTimeStamp();
        this.severity = n.getSeverity();
        this.sequenceNumber = n.getSequenceNumber();
        this.category = category;
    }

    public static JsonNotification parse(final Gson jsonFormatter, final String notification){
        return jsonFormatter.fromJson(notification, JsonNotification.class);
    }

    public final String toString(final Gson jsonFormatter){
        return jsonFormatter.toJson(this);
    }

    public final String getMessage() {
        return message;
    }

    public final void setMessage(final String value) {
        this.message = value;
    }

    public final Date getTimeStamp() {
        return timeStamp;
    }

    public final void setTimeStamp(final Date value) {
        this.timeStamp = value;
    }

    public final Severity getSeverity() {
        return severity;
    }

    public final void setSeverity(final Severity value) {
        this.severity = value;
    }

    public final long getSequenceNumber() {
        return sequenceNumber;
    }

    public final void setSequenceNumber(final long value) {
        this.sequenceNumber = value;
    }

    public final String getCategory() {
        return category;
    }

    public final void setCategory(final String value) {
        this.category = value;
    }
}
