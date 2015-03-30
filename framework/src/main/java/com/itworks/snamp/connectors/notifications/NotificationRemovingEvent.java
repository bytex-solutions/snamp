package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.connectors.FeatureRemovingEvent;
import com.itworks.snamp.internal.Utils;

import javax.management.MBeanNotificationInfo;

/**
 * Represents an event raised by managed resource connector when
 * notification was removed from it.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class NotificationRemovingEvent extends FeatureRemovingEvent<MBeanNotificationInfo> {
    private static final long serialVersionUID = 1424088584234471771L;

    public NotificationRemovingEvent(final NotificationSupport sender,
                                     final String resourceName,
                                     final MBeanNotificationInfo removedFeature) {
        super(sender, resourceName, removedFeature);
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
