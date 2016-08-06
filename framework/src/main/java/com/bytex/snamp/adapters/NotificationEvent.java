package com.bytex.snamp.adapters;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.EventObject;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class NotificationEvent extends EventObject {
    private static final long serialVersionUID = 3190524304810877055L;
    private final Notification notification;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public NotificationEvent(final MBeanNotificationInfo source,
                             final Notification notification) {
        super(source);
        this.notification = Objects.requireNonNull(notification);
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
    public final MBeanNotificationInfo getSource() {
        assert source instanceof MBeanNotificationInfo: source;
        return (MBeanNotificationInfo) source;
    }
}
