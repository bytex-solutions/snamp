package com.bytex.snamp.connector.notifications.measurement;

import javax.management.Notification;

/**
 * Represents advanced monitoring event.
 * @since 2.0
 * @version 2.0
 */
public abstract class MeasurementNotification extends Notification {
    private static final long serialVersionUID = -5747719139937442378L;

    MeasurementNotification(final String type,
                            final String componentName,
                            final String instanceName,
                            final String message) {
        super(type, new MeasurementSource(componentName, instanceName), 0L, message);
    }

    /**
     * Gets the source of this span.
     *
     * @return The source of this span.
     */
    @Override
    public final MeasurementSource getSource() {
        return (MeasurementSource) super.getSource();
    }

    private void setSource(final MeasurementSource value){
        super.setSource(value);
    }

    public final void setSource(final String componentName, final String instanceName){
        setSource(new MeasurementSource(componentName, instanceName));
    }

    @Override
    public final void setSource(final Object value) {
        setSource((MeasurementSource) value);
    }
}
