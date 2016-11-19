package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.md.notifications.IntegerMeasurementNotification;
import com.bytex.snamp.connector.metrics.RatedGauge64Recorder;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_GAUGE_64_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGauge64;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RatedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class Gauge64Attribute extends MetricHolderAttribute<RatedGauge64Recorder, IntegerMeasurementNotification> {
    static final CompositeType TYPE = RATED_GAUGE_64_TYPE;
    static final String NAME = "gauge64";
    private static final long serialVersionUID = -5234028741040752357L;

    Gauge64Attribute(final String name, final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(IntegerMeasurementNotification.class, name, TYPE, descriptor, RatedGauge64Recorder::new);
    }

    @Override
    CompositeData getValue(final RatedGauge64Recorder metric) {
        return fromRatedGauge64(metric);
    }

    @Override
    void updateMetric(final RatedGauge64Recorder metric, final IntegerMeasurementNotification notification) {
        metric.updateValue(notification);
    }

    @Override
    protected boolean isNotificationEnabled(final IntegerMeasurementNotification notification) {
        return representsMeasurement(notification);
    }
}
