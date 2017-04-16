package com.bytex.snamp.gateway;

import javax.annotation.Nonnull;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class NotificationEvent extends EventObject {
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
        super(resourceName);
        this.notification = notification;
        this.resourceName = resourceName;
        this.metadata = metadata;
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

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final Object getSource() {
        return notification.getSource();
    }
}
