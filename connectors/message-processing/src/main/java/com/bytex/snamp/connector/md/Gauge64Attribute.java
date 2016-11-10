package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedGauge64Recorder;
import com.bytex.snamp.connector.notifications.measurement.InstantMeasurementNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_GAUGE_64_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGauge64;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RatedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class Gauge64Attribute extends MetricHolderAttribute<RatedGauge64Recorder, InstantMeasurementNotification> {
    static final CompositeType TYPE = RATED_GAUGE_64_TYPE;
    static final String NAME = "gauge64";
    private static final long serialVersionUID = -5234028741040752357L;

    Gauge64Attribute(final String name, final AttributeDescriptor descriptor) {
        super(InstantMeasurementNotification.class, name, TYPE, descriptor, RatedGauge64Recorder::new);
    }

    @Override
    CompositeData getValue(final RatedGauge64Recorder metric) {
        return fromRatedGauge64(metric);
    }

    @Override
    void updateMetric(final RatedGauge64Recorder metric, final InstantMeasurementNotification notification) {
        if (notification.isInteger())
            metric.updateValue(x -> notification.applyAsLong(x).orElse(x));
    }
}
