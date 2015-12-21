package com.bytex.snamp.connectors.notifications;

import com.google.common.base.MoreObjects;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Objects;

/**
 * Represents simplified version of {@link javax.management.MBeanNotificationInfo}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CustomNotificationInfo extends MBeanNotificationInfo implements NotificationDescriptorRead {
    private static final long serialVersionUID = 414016119605849730L;
    private final NotificationDescriptor descriptor;

    /**
     * Constructs an <CODE>MBeanNotificationInfo</CODE> object.
     *
     * @param notifType  The name of the notification that can be produced by managed resource.
     * @param description A human readable description of the data.
     * @param descriptor  The descriptor for the notifications.  This may be null
     *                    which is equivalent to an empty descriptor.
     */
    public CustomNotificationInfo(final String notifType,
                                  final String description,
                                  final NotificationDescriptor descriptor) {
        super(new String[]{notifType}, Notification.class.getName(), descriptor.getDescription(description), descriptor);
        this.descriptor = Objects.requireNonNull(descriptor);
    }

    /**
     * Returns the descriptor for the feature.  Changing the returned value
     * will have no affect on the original descriptor.
     *
     * @return a descriptor that is either immutable or a copy of the original.
     */
    @Override
    public final NotificationDescriptor getDescriptor() {
        return MoreObjects.firstNonNull(descriptor, NotificationDescriptor.EMPTY_DESCRIPTOR);
    }
}
