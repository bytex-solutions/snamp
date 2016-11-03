package com.bytex.snamp.connector.notifications.measurement;

import javax.management.Notification;
import java.util.Optional;

/**
 * Represents notification with measurement.
 * @since 2.0
 * @version 2.0
 */
public abstract class Measurement extends Notification {
    private static final long serialVersionUID = -5747719139937442378L;

    Measurement(final String type,
                final String componentName,
                final String instanceName,
                final String message) {
        super(type, new NotificationSource(componentName, instanceName), 0L, message);
    }

    public final void setSource(final String componentName, final String instanceName){
        super.setSource(new NotificationSource(componentName, instanceName));
    }

    public final <T> Optional<T> getSource(final Class<T> sourceType){
        final Object source = getSource();
        return sourceType.isInstance(source) ? Optional.of(sourceType.cast(source)) : Optional.empty();
    }
}
