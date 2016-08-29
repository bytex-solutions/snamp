package com.bytex.snamp.connector.notifications.advanced;

import javax.management.Notification;

/**
 * Represents advanced monitoring event.
 * @since 2.0
 * @version 2.0
 */
public abstract class MonitoringNotification extends Notification {
    MonitoringNotification(final String type,
                           final String componentName,
                           final String instanceName,
                           final long sequenceNumber,
                           final String message) {
        super(type, new MonitoringNotificationSource(componentName, instanceName), sequenceNumber, message);
    }

    /**
     * Gets the source of this span.
     *
     * @return The source of this span.
     */
    @Override
    public final MonitoringNotificationSource getSource() {
        return (MonitoringNotificationSource) super.getSource();
    }

    private void setSource(final MonitoringNotificationSource value){
        super.setSource(value);
    }

    public final void setSource(final String componentName, final String instanceName){
        setSource(new MonitoringNotificationSource(componentName, instanceName));
    }

    @Override
    public final void setSource(final Object value) {
        setSource((MonitoringNotificationSource) value);
    }
}
