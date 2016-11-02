package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedFlag;
import com.bytex.snamp.connector.metrics.RatedFlagRecorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.*;

/**
 * Represents attribute which exposes {@link com.bytex.snamp.connector.metrics.RatedFlag}.
 */
final class FlagAttribute extends MetricHolderAttribute<RatedFlagRecorder> {
    static final CompositeType TYPE = RATED_FLAG_TYPE;
    static final String NAME = "flag";
    private static final long serialVersionUID = -5234028741040752357L;

    FlagAttribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE, descriptor, RatedFlagRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedFlagRecorder metric) {
        return fromRatedFlag(metric);
    }

    private static boolean updateMetric(final RatedFlagRecorder metric, final ValueChangedNotification notification) {
        final boolean success;
        if (success = notification.isFlag())
            metric.updateValue(current -> notification.applyAsBoolean(current).orElse(current));
        return success;
    }

    @Override
    boolean updateMetric(final RatedFlagRecorder metric, final MeasurementNotification notification) {
        return notification instanceof ValueChangedNotification && updateMetric(metric, (ValueChangedNotification) notification);
    }
}
