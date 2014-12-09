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

        private AdditionalNotificationInfo(final String res, final String ev) {
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
    public boolean handle(final SshNotificationView metadata,
                          final Notification notif) {
        if (filter.isAllowed(metadata.getResourceName(), metadata.getEventName(), notif)) {
            notif.setUserData(new AdditionalNotificationInfo(metadata.getResourceName(), metadata.getEventName()));
            return offer(notif);
        } else return false;
    }

    static AdditionalNotificationInfo getAdditionalInfo(final Notification notif){
        return notif != null ? Utils.safeCast(notif.getUserData(), AdditionalNotificationInfo.class) : null;
    }
}
