package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RangedGaugeFPRecorder;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRangedFP;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RangedGaugeFP} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class RangedGaugeFPAttribute extends MetricHolderAttribute<RangedGaugeFPRecorder, ValueChangedNotification> {
    static final CompositeType TYPE = RANGED_GAUGE_FP_TYPE;
    static final String NAME = "rangedGaugeFP";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedGaugeFPAttribute(final String name, final AttributeDescriptor descriptor, final double rangeStart, final double rangeEnd){
        super(ValueChangedNotification.class, name, TYPE, descriptor, (n) -> new RangedGaugeFPRecorder(n, rangeStart, rangeEnd));
    }

    RangedGaugeFPAttribute(final String name, final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
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
    void updateMetric(RangedGaugeFPRecorder metric, ValueChangedNotification notification) {
        if (notification.isFloatingPoint())
            metric.updateValue(x -> notification.applyAsDouble(x).orElse(x));
    }
}
