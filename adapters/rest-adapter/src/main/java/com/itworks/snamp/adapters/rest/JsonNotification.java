package com.itworks.snamp.adapters.rest;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
    private String correlationID;
    private JsonElement attachment;

    public JsonNotification(){
        message = "";
        timeStamp = new Date();
        severity = Severity.UNKNOWN;
        sequenceNumber = 0L;
        category = "";
        correlationID = null;
        attachment = null;
    }

    JsonNotification(final Notification n,
                     final String category,
                     final Function<Object, JsonElement> attachmentConverter) {
        this.message = n.getMessage();
        this.timeStamp = n.getTimeStamp();
        this.severity = n.getSeverity();
        this.sequenceNumber = n.getSequenceNumber();
        this.category = category;
        this.correlationID = n.getCorrelationID();
        this.attachment = attachmentConverter != null ? attachmentConverter.apply(n.getAttachment()) :
                null;
    }

    public void setAttachment(final JsonElement attachment){
        this.attachment = attachment;
    }

    public JsonElement getAttachment(){
        return attachment;
    }

    static JsonNotification parse(final Gson jsonFormatter, final String notification){
        return jsonFormatter.fromJson(notification, JsonNotification.class);
    }

    public String toString(final Gson jsonFormatter){
        return jsonFormatter.toJson(this);
    }

    public String getCorrelationID(){
        return correlationID;
    }

    public void setCorrelationID(final String value){
        correlationID = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String value) {
        this.message = value;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final Date value) {
        this.timeStamp = value;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(final Severity value) {
        this.severity = value;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final long value) {
        this.sequenceNumber = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String value) {
        this.category = value;
    }
}
