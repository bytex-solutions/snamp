package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;

/**
 * Represents descriptor for {@link SpanNotification}.
 */
final class SpanNotificationInfo extends SyntheticNotificationInfo {
    private static final long serialVersionUID = 6070755553933773681L;

    SpanNotificationInfo(final NotificationDescriptor descriptor) {
        super(SpanNotification.TYPE, SpanNotification.class, "Occurs when span will be occurred", descriptor);
    }
}
