package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedTimeRecorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.StopwatchNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.*;

/**
 * Collects timing measurements.
 */
final class TimerAttribute extends MetricHolderAttribute<RatedTimeRecorder> {
    static final CompositeType TYPE = RATED_TIMER_TYPE;
    static final String NAME = "timer";
    private static final long serialVersionUID = -5234028741040752357L;

    TimerAttribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE, descriptor, RatedTimeRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedTimeRecorder metric) {
        return fromRatedTimer(metric);
    }

    private static void updateMetric(final RatedTimeRecorder metric, final StopwatchNotification notification){
        metric.accept(notification.getDuration());
    }

    @Override
    boolean updateMetric(final RatedTimeRecorder metric, final MeasurementNotification notification) {
        final boolean success;
        if (success = notification instanceof StopwatchNotification)
            updateMetric(metric, (StopwatchNotification) notification);
        return success;
    }
}
