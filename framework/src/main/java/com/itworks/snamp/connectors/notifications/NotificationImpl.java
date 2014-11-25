package com.itworks.snamp.connectors.notifications;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents default implementation of the SNAMP notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationImpl implements Notification, Serializable {
    private final long sequenceNumber;
    private final Date timeStamp;
    private final Severity severity;
    private final String message;
    private transient volatile Object userData;
    private final Object attachment;
    private final String correlationID;
    private final Notification next;

    /**
     * Initializes a new notification.
     * @param sev The notification severity.
     * @param sequenceNumber The notification sequence number.
     * @param timeStamp Timestamp of the notification.
     * @param message Human-readable description of the notification.
     * @param correlationID Correlation identifier.
     * @param attachment Notification attachment.
     * @param next The factory of the next notification in the chain.
     */
    public NotificationImpl(final Severity sev,
                            final long sequenceNumber,
                            final Date timeStamp,
                            final String message,
                            final String correlationID,
                            final Object attachment,
                            final Supplier<? extends Notification> next){
        this.sequenceNumber = sequenceNumber;
        this.timeStamp = timeStamp != null ? timeStamp : new Date();
        this.severity = sev != null ? sev : Severity.UNKNOWN;
        this.message = message != null ? message : "";
        userData = null;
        this.attachment = attachment;
        this.correlationID = correlationID;
        this.next = next != null ? next.get() : null;
    }

    /**
     * Initializes a new notification.
     * @param sev The notification severity.
     * @param sequenceNumber The notification sequence number.
     * @param timeStamp Timestamp of the notification.
     * @param message Human-readable description of the notification.
     * @param correlationID Correlation identifier.
     * @param attachment Notification attachment.
     */
    public NotificationImpl(final Severity sev,
                            final long sequenceNumber,
                            final Date timeStamp,
                            final String message,
                            final String correlationID,
                            final Object attachment){
        this(sev, sequenceNumber, timeStamp, message, correlationID, attachment, null);
    }

    /**
     * Initializes a new notification.
     * @param sev The notification severity.
     * @param sequenceNumber The notification sequence number.
     * @param timeStamp Timestamp of the notification.
     * @param message Human-readable description of the notification.
     * @param correlationID Correlation identifier.
     */
    public NotificationImpl(final Severity sev,
                            final long sequenceNumber,
                            final Date timeStamp,
                            final String message,
                            final String correlationID){
        this(sev, sequenceNumber, timeStamp, message, correlationID, null);
    }

    /**
     * Initializes a new notification without attachments.
     * @param sev The notification severity.
     * @param sequenceNumber The notification sequence number.
     * @param timeStamp Timestamp of the notification.
     * @param message Human-readable description of the notification.
     */
    public NotificationImpl(final Severity sev,
                            final long sequenceNumber,
                            final Date timeStamp,
                            final String message){
        this(sev, sequenceNumber, timeStamp, message, null);
    }

    /**
     * Initializes a new notification and copy all properties from the specified notification.
     * @param other The notification to copy.
     */
    public NotificationImpl(final Notification other) {
        this(other.getSeverity(),
                other.getSequenceNumber(),
                other.getTimeStamp(),
                other.getMessage(),
                other.getCorrelationID(),
                other.getNext());
    }

    private static Supplier<NotificationImpl> getNext(final Notification next,
                                            final Function<Notification, NotificationImpl> converter) {
        return next != null && converter != null ?
                Suppliers.ofInstance(converter.apply(next)) :
                null;
    }

    /**
     * Initializes a new notification and copy all properties from the specified notification
     * including chain of correlated notifications.
     * @param other The notification to copy.
     * @param converter The converter that is used to copy chained notifications.
     */
    protected NotificationImpl(final Notification other,
                               final Function<Notification, NotificationImpl> converter) {
        this(other.getSeverity(),
                other.getSequenceNumber(),
                other.getTimeStamp(),
                other.getMessage(),
                other.getCorrelationID(),
                other.getAttachment(),
                getNext(other.getNext(), converter));
    }

    /**
     * Gets correlation identifier associated with this notification.
     * <p/>
     * Correlation identifier helps to associate a set of notifications with the single business activity or event.
     *
     * @return The correlation identifier; or {@literal null}, if identifier is not supplied.
     * @see #getNext()
     */
    @Override
    public final String getCorrelationID() {
        return correlationID;
    }

    /**
     * Gets the date and time at which the notification is generated.
     *
     * @return The date and time at which the notification is generated.
     */
    @Override
    public final Date getTimeStamp() {
        return new Date(timeStamp.getTime());
    }

    /**
     * Gets the order number of the notification message.
     *
     * @return The order number of the notification message.
     */
    @Override
    public final long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Gets a severity of this event.
     *
     * @return The severity of this event.
     */
    @Override
    public final Severity getSeverity() {
        return severity;
    }

    /**
     * Gets a message description of this notification.
     *
     * @return The message description of this notification.
     */
    @Override
    public final String getMessage() {
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

    /**
     * Gets attachment associated with this notification.
     * <p/>
     *
     * @return An attachment associated with this notification; or {@literal null} if no attachment present.
     */
    @Override
    public final Object getAttachment() {
        return attachment;
    }

    /**
     * Gets next notification in the chain.
     * <p/>
     * If resource connector too smart (or have an its own correlation manager)
     * then it able to combine two or more correlated notifications in the chain.
     * This method allows to iterate correlated notifications like a linked list.
     *
     * @return The next notification in the chain; or {@literal null} if this is a last notification
     * in the chain of correlated notifications.
     */
    @Override
    public final Notification getNext() {
        return next;
    }
}
