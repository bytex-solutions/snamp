package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.instrumentation.measurements.jmx.TimeMeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Represents descriptor for notification of type {@link TimeMeasurementNotification}.
 */
final class TimeMeasurementNotificationInfo extends SyntheticNotificationInfo {
    private static final long serialVersionUID = 8362854788317171497L;

    TimeMeasurementNotificationInfo(final NotificationDescriptor descriptor) throws InvalidSyntaxException {
        super(TimeMeasurementNotification.TYPE, TimeMeasurementNotification.class, "Occurs when time measurement will be supplied", descriptor);
    }
}
