package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.UserDataSupport;

import java.util.Date;

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
public interface Notification extends UserDataSupport<Object> {

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
    Severity getSeverity();

    /**
     * Gets a message description of this notification.
     * @return The message description of this notification.
     */
    String getMessage();

    /**
     * Gets attachment associated with this notification.
     * <p>
     *  If attachment type cannot be determined statically (via {@link NotificationMetadata#getAttachmentType()}
     *  then this method may return self-descriptive {@link com.itworks.snamp.connectors.ManagedEntityValue} object
     *  which contains dynamically defined attachment type.
     * @return An attachment associated with this notification; or {@literal null} if no attachment present.
     * @see com.itworks.snamp.connectors.ManagedEntityValue
     */
    Object getAttachment();

    /**
     * Gets correlation identifier associated with this notification.
     * <p>
     *     Correlation identifier helps to associate a set of notifications with the single business activity or event.
     * @return The correlation identifier; or {@literal null}, if identifier is not supplied.
     * @see #getNext()
     */
    String getCorrelationID();

    /**
     * Gets next notification in the chain.
     * <p>
     *     If resource connector too smart (or have an its own correlation manager)
     *     then it able to combine two or more correlated notifications in the chain.
     *     This method allows to iterate over correlated notifications using linked-list style.
     * </p>
     * <p>
     *     Each notification in the chain should have the same correlation identifier
     *     ({@literal null} is allowed) and the same attachment type.
     * </p>
     * @return The next notification in the chain; or {@literal null} if this is a last notification
     * in the chain of correlated notifications.
     */
    Notification getNext();
}
