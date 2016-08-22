package com.bytex.snamp.connector.composite;

import com.bytex.snamp.ArrayUtils;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeNotification extends MBeanNotificationInfo implements CompositeFeature {
    private static final long serialVersionUID = -5930820122739652304L;
    private final String connectorType;

    CompositeNotification(final String connectorType, final MBeanNotificationInfo info){
        super(attachPrefix(connectorType, info.getNotifTypes()), info.getName(), info.getDescription(), info.getDescriptor());
        this.connectorType = connectorType;
    }

    private static String[] attachPrefix(final String connectorType, String[] notifTypes){
        notifTypes = notifTypes.clone();
        for(int i = 0; i < notifTypes.length; i++)
            notifTypes[i] = connectorType + ':' + notifTypes[i];
        return notifTypes;
    }

    String getNotifType(){
        return ArrayUtils.getFirst(getNotifTypes());
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }
}
