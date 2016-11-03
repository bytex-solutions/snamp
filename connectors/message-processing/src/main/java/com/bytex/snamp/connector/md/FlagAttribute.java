package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedFlagRecorder;
import com.bytex.snamp.connector.notifications.measurement.InstantMeasurement;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_FLAG_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedFlag;

/**
 * Represents attribute which exposes {@link com.bytex.snamp.connector.metrics.RatedFlag}.
 */
final class FlagAttribute extends MetricHolderAttribute<RatedFlagRecorder, InstantMeasurement> {
    static final CompositeType TYPE = RATED_FLAG_TYPE;
    static final String NAME = "flag";
    private static final long serialVersionUID = -5234028741040752357L;

    FlagAttribute(final String name, final AttributeDescriptor descriptor) {
        super(InstantMeasurement.class, name, TYPE, descriptor, RatedFlagRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedFlagRecorder metric) {
        return fromRatedFlag(metric);
    }

    @Override
    void updateMetric(final RatedFlagRecorder metric, final InstantMeasurement notification) {
        if (notification.isFlag())
            metric.updateValue(current -> notification.applyAsBoolean(current).orElse(current));
    }
}
