package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RangedGaugeFPRecorder;
import com.bytex.snamp.instrumentation.measurements.FloatingPointMeasurement;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRangedFP;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RangedGaugeFP} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class RangedGaugeFPAttribute extends MetricHolderAttribute<RangedGaugeFPRecorder, ValueMeasurementNotification> {
    static final CompositeType TYPE = RANGED_GAUGE_FP_TYPE;
    static final String NAME = "rangedGaugeFP";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedGaugeFPAttribute(final String name, final AttributeDescriptor descriptor, final double rangeStart, final double rangeEnd) throws InvalidSyntaxException {
        super(ValueMeasurementNotification.class, name, TYPE, descriptor, (n) -> new RangedGaugeFPRecorder(n, rangeStart, rangeEnd));
    }

    RangedGaugeFPAttribute(final String name, final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException, InvalidSyntaxException {
        this(name,
                descriptor,
                DataStreamConnectorConfigurationDescriptionProvider.parseRangeStartAsDouble(descriptor),
                DataStreamConnectorConfigurationDescriptionProvider.parseRangeEndAsDouble(descriptor));
    }

    @Override
    CompositeData getValue(final RangedGaugeFPRecorder metric) {
        return fromRangedFP(metric);
    }

    private static void updateMetric(final RangedGaugeFPRecorder metric, final FloatingPointMeasurement measurement){
        metric.updateValue(measurement::getValue);
    }

    @Override
    void updateMetric(final RangedGaugeFPRecorder metric, final ValueMeasurementNotification notification) {
        extractMeasurementAndUpdateMetric(notification, FloatingPointMeasurement.class, metric, RangedGaugeFPAttribute::updateMetric);
    }

    @Override
    protected boolean isNotificationEnabled(final ValueMeasurementNotification notification) {
        return representsMeasurement(notification) && notification.isMeasurement(FloatingPointMeasurement.class);
    }
}
