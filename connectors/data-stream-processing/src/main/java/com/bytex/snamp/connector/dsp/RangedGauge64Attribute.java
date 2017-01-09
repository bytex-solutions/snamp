package com.bytex.snamp.connector.dsp;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.dsp.notifications.ValueMeasurementNotification;
import com.bytex.snamp.connector.metrics.RangedGauge64Recorder;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
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
final class RangedGauge64Attribute extends MetricHolderAttribute<RangedGauge64Recorder, ValueMeasurementNotification> {
    static final CompositeType TYPE = RANGED_GAUGE_64_TYPE;
    static final String NAME = "rangedGauge64";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedGauge64Attribute(final String name, final AttributeDescriptor descriptor, final long rangeStart, final long rangeEnd) throws InvalidSyntaxException {
        super(ValueMeasurementNotification.class, name, TYPE, descriptor, (n) -> new RangedGauge64Recorder(n, rangeStart, rangeEnd));
    }

    RangedGauge64Attribute(final String name, final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException, InvalidSyntaxException {
        this(name,
                descriptor,
                DataStreamDrivenConnectorConfigurationDescriptionProvider.parseRangeStartAsLong(descriptor),
                DataStreamDrivenConnectorConfigurationDescriptionProvider.parseRangeEndAsLong(descriptor));
    }

    @Override
    CompositeData getValue(final RangedGauge64Recorder metric) {
        return fromRatedGauge64(metric);
    }

    @Override
    void updateMetric(final RangedGauge64Recorder metric, final ValueMeasurementNotification notification) {
        notification.getMeasurement(IntegerMeasurement.class).ifPresent(measurement -> metric.updateValue(measurement::getValue));
    }

    @Override
    protected boolean isNotificationEnabled(final ValueMeasurementNotification notification) {
        return representsMeasurement(notification) && notification.isMeasurement(IntegerMeasurement.class);
    }
}
