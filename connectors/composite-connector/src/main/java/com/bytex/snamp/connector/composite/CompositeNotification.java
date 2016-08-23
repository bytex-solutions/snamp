package com.bytex.snamp.connector.composite;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Box;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeNotification extends MBeanNotificationInfo implements CompositeFeature {
    private static final long serialVersionUID = -5930820122739652304L;
    private final String connectorType;
    private final String shortName;

    CompositeNotification(final String notifType, final MBeanNotificationInfo info) {
        super(new String[]{notifType}, info.getName(), info.getDescription(), info.getDescriptor());
        final Box<String> connectorType = new Box<>();
        final Box<String> type = new Box<>();
        if (ConnectorTypeSplit.split(notifType, connectorType, type)) {
            this.connectorType = connectorType.get();
            this.shortName = type.get();
        } else
            throw invalidNotificationType(notifType);
    }

    static IllegalArgumentException invalidNotificationType(final String notifType){
        return new IllegalArgumentException("Invalid notification type " + notifType);
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }
}
