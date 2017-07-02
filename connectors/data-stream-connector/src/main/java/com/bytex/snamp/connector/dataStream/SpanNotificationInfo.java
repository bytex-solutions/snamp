package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Represents descriptor for {@link SpanNotification}.
 */
final class SpanNotificationInfo extends SyntheticNotificationInfo {
    SpanNotificationInfo(final NotificationDescriptor descriptor) throws InvalidSyntaxException {
        super(SpanNotification.TYPE, SpanNotification.class, "Occurs when span will be occurred", descriptor);
    }
}
