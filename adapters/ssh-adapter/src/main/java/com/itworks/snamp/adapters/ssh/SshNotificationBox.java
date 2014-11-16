package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationBox;
import com.itworks.snamp.connectors.notifications.NotificationFilter;
import com.itworks.snamp.internal.Utils;

import java.util.Objects;

/**
 * Represents notification box that supports filtering.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshNotificationBox extends NotificationBox implements NotificationListener {
    private final NotificationFilter filter;

    static final class AdditionalNotificationInfo {
        public final String eventName;
        public final String resourceName;

        AdditionalNotificationInfo(final String ev, final String res) {
            this.resourceName = res;
            this.eventName = ev;
        }
    }

    SshNotificationBox(final int maxCapacity,
                       final NotificationFilter filter){
        super(maxCapacity);
        this.filter = Objects.requireNonNull(filter, "filter is null.");
    }

    @Override
    public boolean handle(final String resourceName, final String eventName, final Notification notif) {
        if (filter.isAllowed(resourceName, eventName, notif)) {
            notif.setUserData(new AdditionalNotificationInfo(eventName, resourceName));
            return offer(notif);
        } else return false;
    }

    static AdditionalNotificationInfo getAdditionalInfo(final Notification notif){
        return notif != null ? Utils.safeCast(notif.getUserData(), AdditionalNotificationInfo.class) : null;
    }
}
