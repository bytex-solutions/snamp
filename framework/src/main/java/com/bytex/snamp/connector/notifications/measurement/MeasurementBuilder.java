package com.bytex.snamp.connector.notifications.measurement;

import com.bytex.snamp.connector.notifications.AbstractNotificationBuilder;

/**
 * Represents builder for measurement events based on class {@link MeasurementNotification}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class MeasurementBuilder<N extends MeasurementNotification> extends AbstractNotificationBuilder<N> {

    public final MeasurementBuilder<N> setSource(final String componentName, final String instanceName){
        setSource(new NotificationSource(componentName, instanceName));
        return this;
    }
}
