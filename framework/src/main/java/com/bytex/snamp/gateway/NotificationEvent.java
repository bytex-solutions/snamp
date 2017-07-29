package com.bytex.snamp.gateway;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.io.SerializationMode;

import javax.annotation.Nonnull;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.io.Serializable;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class NotificationEvent extends Notification {
    private static final long serialVersionUID = 3190524304810877055L;
    private final Notification notification;
    private final String resourceName;
    private final MBeanNotificationInfo metadata;

    /**
     * Constructs a prototypical Event.
     *
     * @param metadata The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public NotificationEvent(@Nonnull final String resourceName,
                             @Nonnull final MBeanNotificationInfo metadata,
                             @Nonnull final Notification notification) {
        super(notification.getType(), resourceName, notification.getSequenceNumber());
        this.notification = notification;
        this.resourceName = resourceName;
        this.metadata = metadata;
    }

    @Override
    public final long getSequenceNumber() {
        return notification.getSequenceNumber();
    }

    @Override
    public final void setSequenceNumber(final long value) {
        notification.setSequenceNumber(value);
    }

    @Override
    public final String getType() {
        return notification.getType();
    }

    @Override
    public final long getTimeStamp() {
        return notification.getTimeStamp();
    }

    @Override
    public void setTimeStamp(final long value) {
        notification.setTimeStamp(value);
    }

    @Override
    public final String getMessage() {
        return notification.getMessage();
    }

    @Override
    public final Object getUserData() {
        return notification.getUserData();
    }

    @Override
    public final void setUserData(final Object value) {
        notification.setUserData(value);
    }

    public final String getResourceName(){
        return resourceName;
    }

    /**
     * Gets metadata of the notification.
     * @return Notification metadata.
     */
    public final MBeanNotificationInfo getMetadata(){
        return metadata;
    }

    /**
     * Gets notification associated with this event.
     * @return The notification associated with this event.
     */
    public final Notification getNotification(){
        return notification;
    }


    private static Notification cloneNotification(final Notification notification,
                                                 final Serializable newSource) {
        //type of replacement aligned with method AbstractNotificationRepository.setSource.
        final SerializationMode mode = SerializationMode.objectReplacement(Aggregator.class, value -> newSource);
        return callUnchecked(() -> IOUtils.clone(notification, mode));
    }

    /**
     * Creates deep clone of notification.
     * @return Deeply cloned notification.
     */
    public final Notification cloneNotification() {
        return cloneNotification(notification, resourceName);
    }

    @Override
    public final Object getSource() {
        return notification.getSource();
    }

    @Override
    public final void setSource(final Object value) {
        notification.setSource(value);
    }

    @Override
    public String toString() {
        return notification.toString();
    }
}
