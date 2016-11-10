package com.bytex.snamp.connector.notifications.measurement;

import javax.management.Notification;
import java.util.Optional;

/**
 * Represents notification with measurement.
 * @since 2.0
 * @version 2.0
 */
public abstract class MeasurementNotification extends Notification {
    private static final long serialVersionUID = -5747719139937442378L;

    MeasurementNotification(final String type,
                            final Object source,
                            final String message) {
        super(type, source, 0L, message);
    }

    public final void setSource(final NotificationSource source){
        super.setSource(source);
    }

    public final <T> Optional<T> getSource(final Class<T> sourceType){
        final Object source = getSource();
        return sourceType.isInstance(source) ? Optional.of(sourceType.cast(source)) : Optional.empty();
    }
}
