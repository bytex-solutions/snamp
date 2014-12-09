package com.itworks.snamp.connectors.notifications;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.internal.Utils;
import org.osgi.service.event.Event;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Represents SNAMP notification constructed from {@link org.osgi.service.event.Event} object.
 * <p>
 *     This class represents transport unit using for delivering notifications
 *     emitted by managed resource across OSGi environment.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class NotificationEvent implements Notification, Serializable {
    private static final String WRAPPED_NOTIF_EVENT_PROPERTY = "notification";
    private static final String LIST_EVENT_PROPERTY = "subscriptionListID";
    private static final String SENDER_EVENT_PROPERTY = "sender";

    private final String subscriptionList;
    private final String sender;
    private final Notification wrappedNotification;

    /**
     * Initializes a new instance of the SNAMP notification using {@link org.osgi.service.event.Event} object.
     *
     * @param ev An event to parse.
     */
    public NotificationEvent(final Event ev) {
        subscriptionList = Utils.getEventProperty(ev, LIST_EVENT_PROPERTY, String.class, "");
        sender = Utils.getEventProperty(ev, SENDER_EVENT_PROPERTY, String.class, "");
        wrappedNotification = Utils.getEventProperty(ev, WRAPPED_NOTIF_EVENT_PROPERTY, Notification.class, null);
    }

    /**
     * Initializes a new instance of the SNAMP notification.
     *
     * @param notification Original SNAMP notification to wrap.
     * @param resourceName The name of the managed resource which emits the event.
     * @param listId       An identifier of the subscription list.
     */
    public NotificationEvent(final Notification notification,
                             final String resourceName,
                             final String listId) {
        wrappedNotification = notification;
        subscriptionList = listId;
        sender = resourceName;
    }

    /**
     * Gets name of the managed resource which emits this event.
     *
     * @return The name of the manager resource.
     */
    public final String getSender() {
        return sender;
    }


    /**
     * Gets subscription list identifier.
     *
     * @return The subscription list identifier.
     */
    public final String getSubscriptionListID() {
        return subscriptionList;
    }

    /**
     * Wraps notification into {@link org.osgi.service.event.Event} object.
     *
     * @param connectorName The management connector name.
     * @param category      An event category.
     * @return A new instance of the {@link org.osgi.service.event.Event} object that
     * contains properties from this notification.
     */
    public Event toEvent(final String connectorName,
                         final String category) {
        final Map<String, Object> eventProps = ImmutableMap.of(LIST_EVENT_PROPERTY, subscriptionList,
                SENDER_EVENT_PROPERTY, sender,
                WRAPPED_NOTIF_EVENT_PROPERTY, wrappedNotification);
        //attach events
        return new Event(NotificationUtils.getTopicName(connectorName, category, getSubscriptionListID()),
                eventProps);
    }

    /**
     * Gets the date and time at which the notification is generated.
     *
     * @return The date and time at which the notification is generated.
     */
    @Override
    public Date getTimeStamp() {
        return wrappedNotification.getTimeStamp();
    }

    /**
     * Gets the order number of the notification message.
     *
     * @return The order number of the notification message.
     */
    @Override
    public long getSequenceNumber() {
        return wrappedNotification.getSequenceNumber();
    }

    /**
     * Gets a severity of this event.
     *
     * @return The severity of this event.
     */
    @Override
    public Severity getSeverity() {
        return wrappedNotification.getSeverity();
    }

    /**
     * Gets a message description of this notification.
     *
     * @return The message description of this notification.
     */
    @Override
    public String getMessage() {
        return wrappedNotification.getMessage();
    }

    /**
     * Gets attachment associated with this notification.
     * <p/>
     * If attachment type cannot be determined statically (via {@link com.itworks.snamp.connectors.notifications.NotificationMetadata#getAttachmentType()}
     * then this method may return self-descriptive {@link com.itworks.snamp.connectors.ManagedEntityValue} object
     * which contains dynamically defined attachment type.
     *
     * @return An attachment associated with this notification; or {@literal null} if no attachment present.
     * @see com.itworks.snamp.connectors.ManagedEntityValue
     */
    @Override
    public Object getAttachment() {
        return wrappedNotification.getAttachment();
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
    public String getCorrelationID() {
        return wrappedNotification.getCorrelationID();
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
    public Notification getNext() {
        return wrappedNotification.getNext();
    }

    /**
     * Gets user data associated with this object.
     *
     * @return The user data associated with this object.
     */
    @Override
    public Object getUserData() {
        return wrappedNotification.getUserData();
    }

    /**
     * Sets the user data associated with this object.
     *
     * @param value The user data to be associated with this object.
     */
    @Override
    public void setUserData(final Object value) {
        wrappedNotification.setUserData(value);
    }
}
