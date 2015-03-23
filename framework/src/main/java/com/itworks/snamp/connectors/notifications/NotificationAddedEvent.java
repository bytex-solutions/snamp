package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.connectors.FeatureAddedEvent;
import com.itworks.snamp.internal.Utils;

import javax.management.MBeanNotificationInfo;

/**
 * Represents an event raised by managed resource connector was extende
 * with a new notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationAddedEvent extends FeatureAddedEvent<MBeanNotificationInfo> {
    private static final long serialVersionUID = 554956826663151805L;

    public NotificationAddedEvent(final NotificationSupport sender,
                                  final MBeanNotificationInfo addedNotification) {
        super(sender, addedNotification);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final NotificationSupport getSource() {
        return Utils.safeCast(source, NotificationSupport.class);
    }
}
