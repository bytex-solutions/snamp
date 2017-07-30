package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import javax.management.MBeanNotificationInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class CompositeNotification extends MBeanNotificationInfo implements CompositeFeature {
    private static final long serialVersionUID = -5930820122739652304L;
    private final String connectorType;

    CompositeNotification(final String connectorType, final MBeanNotificationInfo info) {
        super(info.getNotifTypes(), info.getName(), NotificationDescriptor.getDescription(info), info.getDescriptor());
        this.connectorType = Objects.requireNonNull(connectorType);
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }
}
