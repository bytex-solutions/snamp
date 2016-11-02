package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.ArrivalsRecorder;
import com.bytex.snamp.connector.notifications.measurement.StopwatchNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.ARRIVALS_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromArrivals;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.Arrivals} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class ArrivalsAttribute extends MetricHolderAttribute<ArrivalsRecorder, StopwatchNotification> {
    static final CompositeType TYPE = ARRIVALS_TYPE;
    static final String NAME = "arrivals";
    private static final long serialVersionUID = -5234028741040752357L;
    private final long channels;

    ArrivalsAttribute(final String name, final AttributeDescriptor descriptor) {
        super(StopwatchNotification.class, name, TYPE, descriptor, ArrivalsRecorder::new);
        channels = MessageDrivenConnectorConfigurationDescriptor.parseChannels(descriptor);
    }

    @Override
    CompositeData getValue(final ArrivalsRecorder metric) {
        return fromArrivals(metric, channels);
    }

    @Override
    void updateMetric(final ArrivalsRecorder metric, final StopwatchNotification notification) {
        metric.accept(notification.getDuration());
    }
}
