package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Represents descriptor for notification of type {@link ValueMeasurementNotification}.
 */
final class ValueMeasurementNotificationInfo extends SyntheticNotificationInfo {
    private static final long serialVersionUID = 128520617064322629L;

    ValueMeasurementNotificationInfo(final NotificationDescriptor descriptor) throws InvalidSyntaxException {
        super(ValueMeasurementNotification.TYPE, ValueMeasurementNotification.class, "Occurs when instant measurement will be supplied", descriptor);
    }
}
