package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.SimpleNotificationInfo;

import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
final class JmxNotificationInfo extends SimpleNotificationInfo implements JmxNotificationMetadata {
    private static final long serialVersionUID = -2040203631422591069L;

    /**
     * Represents owner of this notification metadata.
     */
    private final ObjectName eventOwner;

    JmxNotificationInfo(final String listID,
                        final MBeanNotificationInfo nativeNotif,
                        final ObjectName eventOwner,
                        final NotificationDescriptor descriptor) {
        super(listID,
                descriptor.getDescription(nativeNotif.getDescription()),
                descriptor.setFields(nativeNotif.getDescriptor()));
        this.eventOwner = eventOwner;
    }

    @Override
    public String getAlias() {
        return NotificationDescriptor.getName(this);
    }

    @Override
    public ObjectName getOwner() {
        return eventOwner;
    }
}
