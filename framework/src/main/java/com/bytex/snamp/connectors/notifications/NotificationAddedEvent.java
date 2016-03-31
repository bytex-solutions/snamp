package com.bytex.snamp.connectors.notifications;

import com.bytex.snamp.connectors.FeatureAddedEvent;

import javax.management.MBeanNotificationInfo;

/**
 * Represents an event raised by managed resource connector was extended
 * with a new set of notifications.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class NotificationAddedEvent extends FeatureAddedEvent<MBeanNotificationInfo> {
    private static final long serialVersionUID = 554956826663151805L;

    public NotificationAddedEvent(final NotificationSupport sender,
                                  final String resourceName,
                                  final MBeanNotificationInfo feature) {
        super(sender, resourceName, feature);
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return The object on which the Event initially occurred.
     */
    @Override
    public final NotificationSupport getSource() {
        assert source instanceof NotificationSupport: source;
        return (NotificationSupport) source;
    }
}
