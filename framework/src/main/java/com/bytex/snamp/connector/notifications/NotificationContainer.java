package com.bytex.snamp.connector.notifications;

import javax.management.Notification;
import java.util.function.Supplier;

/**
 * Represents wrapper for another notification.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class NotificationContainer extends Notification implements Supplier<Notification> {
    private static final long serialVersionUID = -6438758971188748019L;
    private final Notification wrappedNotification;

    private NotificationContainer(final String type, final Notification notification){
        super(type, notification.getSource(), notification.getSequenceNumber());
        wrappedNotification = notification;
    }

    static Notification create(final String type, final Notification notification){
        return notification.getType().equals(type) ? notification : new NotificationContainer(type, notification);
    }

    /**
     * Get the notification sequence number.
     *
     * @return The notification sequence number within the source object. It's a serial number
     * identifying a particular instance of notification in the context of the notification source.
     * The notification model does not assume that notifications will be received in the same order
     * that they are sent. The sequence number helps listeners to sort received notifications.
     * @see #setSequenceNumber
     */
    @Override
    public long getSequenceNumber() {
        return wrappedNotification.getSequenceNumber();
    }

    /**
     * Set the notification sequence number.
     *
     * @param sequenceNumber The notification sequence number within the source object. It is
     *                       a serial number identifying a particular instance of notification in the
     *                       context of the notification source.
     * @see #getSequenceNumber
     */
    @Override
    public void setSequenceNumber(final long sequenceNumber) {
        wrappedNotification.setSequenceNumber(sequenceNumber);
    }

    /**
     * Sets the source.
     *
     * @param source the new source for this object.
     */
    @Override
    public void setSource(final Object source) {
        wrappedNotification.setSource(source);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public Object getSource() {
        return wrappedNotification.getSource();
    }

    /**
     * Get the notification timestamp.
     *
     * @return The notification timestamp.
     * @see #setTimeStamp
     */
    @Override
    public long getTimeStamp() {
        return wrappedNotification.getTimeStamp();
    }

    /**
     * Set the notification timestamp.
     *
     * @param timeStamp The notification timestamp. It indicates when the notification was generated.
     * @see #getTimeStamp
     */
    @Override
    public void setTimeStamp(final long timeStamp) {
        wrappedNotification.setTimeStamp(timeStamp);
    }

    /**
     * Get the notification message.
     *
     * @return The message string of this notification object.
     */
    @Override
    public String getMessage() {
        return wrappedNotification.getMessage();
    }

    /**
     * Get the user data.
     *
     * @return The user data object. It is used for whatever data
     * the notification source wishes to communicate to its consumers.
     * @see #setUserData
     */
    @Override
    public Object getUserData() {
        return wrappedNotification.getUserData();
    }

    /**
     * Set the user data.
     *
     * @param userData The user data object. It is used for whatever data
     *                 the notification source wishes to communicate to its consumers.
     * @see #getUserData
     */
    @Override
    public void setUserData(final Object userData) {
        wrappedNotification.setUserData(userData);
    }

    /**
     * Returns a String representation of this notification.
     *
     * @return A String representation of this notification.
     */
    @Override
    public String toString() {
        return wrappedNotification.toString();
    }

    /**
     * Gets notification wrapped in this container.
     * @return Notification wrapped in this container.
     */
    @Override
    public Notification get(){
        return wrappedNotification;
    }


    @Override
    public int hashCode() {
        return wrappedNotification.hashCode();
    }

    private boolean equals(final NotificationContainer other){
        return other.wrappedNotification.equals(wrappedNotification);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof NotificationContainer && equals((NotificationContainer) other);
    }
}
