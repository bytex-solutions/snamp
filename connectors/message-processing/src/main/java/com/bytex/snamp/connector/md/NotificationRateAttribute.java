package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RateRecorder;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATE_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRate;

/**
 * Measures rate of some event.
 */
final class NotificationRateAttribute extends MetricHolderAttribute<RateRecorder, Notification> {
    static final CompositeType TYPE = RATE_TYPE;
    static final String NAME = "notificationRate";
    private static final long serialVersionUID = -5234028741040752357L;

    NotificationRateAttribute(final String name, final AttributeDescriptor descriptor) {
        super(Notification.class, name, TYPE, descriptor, RateRecorder::new);
    }

    @Override
    CompositeData getValue(final RateRecorder metric) {
        return fromRate(metric);
    }

    @Override
    void updateMetric(final RateRecorder metric, final Notification notification) {
        metric.mark();
    }
}
