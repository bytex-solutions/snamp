package com.bytex.snamp.connector.notifications;

import com.google.common.collect.ImmutableSet;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * Represents notification filter based on the notification type.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class TypeBasedNotificationFilter implements NotificationFilter {
    private static final long serialVersionUID = 5870957878453019509L;
    private final ImmutableSet<String> notifTypes;

    public TypeBasedNotificationFilter(final String... notifTypes){
        this.notifTypes = ImmutableSet.copyOf(notifTypes);
    }

    public TypeBasedNotificationFilter(final MBeanNotificationInfo metadata){
        this(metadata.getNotifTypes());
    }

    /**
     * Invoked before sending the specified notification to the listener.
     *
     * @param notification The notification to be sent.
     * @return <CODE>true</CODE> if the notification has to be sent to the listener, <CODE>false</CODE> otherwise.
     */
    @Override
    public boolean isNotificationEnabled(final Notification notification) {
        return notifTypes.contains(notification.getType());
    }
}
