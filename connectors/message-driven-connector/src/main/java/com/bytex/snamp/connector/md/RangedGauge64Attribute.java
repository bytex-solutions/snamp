package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.md.notifications.IntegerMeasurementNotification;
import com.bytex.snamp.connector.metrics.RangedGauge64Recorder;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_GAUGE_64_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGauge64;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RangedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class RangedGauge64Attribute extends MetricHolderAttribute<RangedGauge64Recorder, IntegerMeasurementNotification> {
    static final CompositeType TYPE = RANGED_GAUGE_64_TYPE;
    static final String NAME = "rangedGauge64";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedGauge64Attribute(final String name, final AttributeDescriptor descriptor, final long rangeStart, final long rangeEnd) throws InvalidSyntaxException {
        super(IntegerMeasurementNotification.class, name, TYPE, descriptor, (n) -> new RangedGauge64Recorder(n, rangeStart, rangeEnd));
    }

    RangedGauge64Attribute(final String name, final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException, InvalidSyntaxException {
        this(name,
                descriptor,
                MessageDrivenConnectorConfigurationDescriptor.parseRangeStartAsLong(descriptor),
                MessageDrivenConnectorConfigurationDescriptor.parseRangeEndAsLong(descriptor));
    }

    @Override
    CompositeData getValue(final RangedGauge64Recorder metric) {
        return fromRatedGauge64(metric);
    }

    @Override
    void updateMetric(final RangedGauge64Recorder metric, final IntegerMeasurementNotification notification) {
        metric.updateValue(notification);
    }

    @Override
    protected boolean isNotificationEnabled(final IntegerMeasurementNotification notification) {
        return representsMeasurement(notification);
    }
}
