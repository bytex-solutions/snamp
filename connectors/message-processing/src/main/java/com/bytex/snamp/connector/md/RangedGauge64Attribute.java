package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RangedGauge64Recorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_GAUGE_64_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGauge64;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RangedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class RangedGauge64Attribute extends MetricHolderAttribute<RangedGauge64Recorder> {
    static final CompositeType TYPE = RANGED_GAUGE_64_TYPE;
    static final String NAME = "rangedGauge64";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedGauge64Attribute(final String name, final AttributeDescriptor descriptor, final long rangeStart, final long rangeEnd){
        super(name, TYPE, descriptor, (n) -> new RangedGauge64Recorder(n, rangeStart, rangeEnd));
    }

    RangedGauge64Attribute(final String name, final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        this(name,
                descriptor,
                MessageDrivenConnectorConfigurationDescriptor.parseRangeStartAsLong(descriptor),
                MessageDrivenConnectorConfigurationDescriptor.parseRangeEndAsLong(descriptor));
    }

    @Override
    CompositeData getValue(final RangedGauge64Recorder metric) {
        return fromRatedGauge64(metric);
    }

    private static boolean updateMetric(final RangedGauge64Recorder metric, final ValueChangedNotification notification) {
        final boolean success;
        if (success = notification.isInteger())
            metric.updateValue(x -> notification.applyAsLong(x).orElse(x));
        return success;
    }

    @Override
    boolean updateMetric(final RangedGauge64Recorder metric, final MeasurementNotification notification) {
        return notification instanceof ValueChangedNotification && updateMetric(metric, (ValueChangedNotification) notification);
    }
}
