package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.md.notifications.FloatingPointMeasurementNotification;
import com.bytex.snamp.connector.metrics.RangedGaugeFPRecorder;
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
final class RangedGaugeFPAttribute extends MetricHolderAttribute<RangedGaugeFPRecorder, FloatingPointMeasurementNotification> {
    static final CompositeType TYPE = RANGED_GAUGE_FP_TYPE;
    static final String NAME = "rangedGaugeFP";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedGaugeFPAttribute(final String name, final AttributeDescriptor descriptor, final double rangeStart, final double rangeEnd) throws InvalidSyntaxException {
        super(FloatingPointMeasurementNotification.class, name, TYPE, descriptor, (n) -> new RangedGaugeFPRecorder(n, rangeStart, rangeEnd));
    }

    RangedGaugeFPAttribute(final String name, final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException, InvalidSyntaxException {
        this(name,
                descriptor,
                MessageDrivenConnectorConfigurationDescriptor.parseRangeStartAsDouble(descriptor),
                MessageDrivenConnectorConfigurationDescriptor.parseRangeEndAsDouble(descriptor));
    }

    @Override
    CompositeData getValue(final RangedGaugeFPRecorder metric) {
        return fromRangedFP(metric);
    }

    @Override
    void updateMetric(final RangedGaugeFPRecorder metric, final FloatingPointMeasurementNotification notification) {
        metric.updateValue(notification);
    }

    @Override
    protected boolean isNotificationEnabled(final FloatingPointMeasurementNotification notification) {
        return representsMeasurement(notification);
    }
}
