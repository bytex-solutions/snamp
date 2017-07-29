package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RateRecorder;
import com.bytex.snamp.jmx.MetricsConverter;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

/**
 * Represents attribute computing rate of the input notifications.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class NotificationRateAttribute extends MetricAttribute<RateRecorder> implements NotificationListener {
    private static final long serialVersionUID = -6525078880291154173L;
    private static final CompositeType TYPE = MetricsConverter.RATE_TYPE;
    private static final String DESCRIPTION = "Computes rate of the notification";
    private final String notificationType;

    NotificationRateAttribute(final String name,
                              final AttributeDescriptor descriptor) {
        super(name, TYPE, DESCRIPTION, descriptor, RateRecorder::new);
        notificationType = descriptor.getAlternativeName().orElse(name);
    }

    @Override
    CompositeData getValue(final RateRecorder metric) {
        return MetricsConverter.fromRate(metric);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if (notificationType.equals(notification.getType()))
            updateMetric(RateRecorder::mark);
    }
}
