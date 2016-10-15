package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.connector.metrics.RateRecorder;
import com.bytex.snamp.jmx.MetricsConverter;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

/**
 * Represents attribute computing rate of the input notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class NotificationRateAttribute extends MetricAttribute<Rate> implements NotificationListener {
    private static final long serialVersionUID = -6525078880291154173L;
    private static final CompositeType TYPE = MetricsConverter.RATE_TYPE;
    private static final String DESCRIPTION = "Computes rate of the notification";

    private volatile RateRecorder rate;
    private final String notificationType;

    NotificationRateAttribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE.getClassName(), descriptor.getDescription(DESCRIPTION), true, false, false, descriptor);
        rate = new RateRecorder(name);
        notificationType = descriptor.getName(name);
    }

    @Override
    Rate getMetric() {
        return rate;
    }

    @Override
    boolean setMetric(final Metric value) {
        final boolean success;
        if (success = value instanceof RateRecorder)
            rate = (RateRecorder) value;
        return success;
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if(this.notificationType.equals(notification.getType()))
            rate.mark();
    }

    @Override
    CompositeData getValue(final AttributeSupportProvider provider) {
        return MetricsConverter.fromRate(rate);
    }
}
