package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.md.notifications.BooleanMeasurementNotification;
import com.bytex.snamp.connector.metrics.RatedFlagRecorder;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_FLAG_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedFlag;

/**
 * Represents attribute which exposes {@link com.bytex.snamp.connector.metrics.RatedFlag}.
 */
final class FlagAttribute extends MetricHolderAttribute<RatedFlagRecorder, BooleanMeasurementNotification> {
    static final CompositeType TYPE = RATED_FLAG_TYPE;
    static final String NAME = "flag";
    private static final long serialVersionUID = -5234028741040752357L;

    FlagAttribute(final String name, final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(BooleanMeasurementNotification.class, name, TYPE, descriptor, RatedFlagRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedFlagRecorder metric) {
        return fromRatedFlag(metric);
    }

    @Override
    void updateMetric(final RatedFlagRecorder metric, final BooleanMeasurementNotification notification) {
        metric.updateValue(notification);
    }
}
