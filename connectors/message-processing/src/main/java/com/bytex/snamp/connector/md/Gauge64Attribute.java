package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedGauge64Recorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;
import com.bytex.snamp.jmx.MetricsConverter;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_GAUGE_64_TYPE;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RatedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class Gauge64Attribute extends MetricHolderAttribute<RatedGauge64Recorder> {
    private static final CompositeType TYPE = RATED_GAUGE_64_TYPE;
    static final String NAME = "gauge64";
    private static final String DESCRIPTION = "Represents 64-bit gauge";
    private static final long serialVersionUID = -5234028741040752357L;

    Gauge64Attribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE, DESCRIPTION, descriptor, RatedGauge64Recorder::new);
    }

    @Override
    CompositeData getValue(final RatedGauge64Recorder metric) {
        return MetricsConverter.fromRatedGauge64(metric);
    }

    private boolean updateMetric(final RatedGauge64Recorder metric, final ValueChangedNotification notification) {
        if (notification.isInteger()) {
            metric.updateValue(x -> notification.apply(x).orElse(x));
            return true;
        } else
            return false;
    }

    @Override
    boolean updateMetric(final RatedGauge64Recorder metric, final MeasurementNotification notification) {
        return notification instanceof ValueChangedNotification && updateMetric(metric, (ValueChangedNotification) notification);
    }
}
