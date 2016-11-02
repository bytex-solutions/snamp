package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RangedTimerRecorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.StopwatchNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.time.Duration;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_TIMER_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRangedTimer;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RangedTimer} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class RangedTimerAttribute extends MetricHolderAttribute<RangedTimerRecorder> {
    static final CompositeType TYPE = RANGED_TIMER_TYPE;
    static final String NAME = "rangedTimer";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedTimerAttribute(final String name, final AttributeDescriptor descriptor, final Duration rangeStart, final Duration rangeEnd){
        super(name, TYPE, descriptor, (n) -> new RangedTimerRecorder(n, rangeStart, rangeEnd));
    }

    RangedTimerAttribute(final String name, final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        this(name,
                descriptor,
                MessageDrivenConnectorConfigurationDescriptor.parseRangeStartAsDuration(descriptor),
                MessageDrivenConnectorConfigurationDescriptor.parseRangeEndAsDuration(descriptor));
    }

    @Override
    CompositeData getValue(final RangedTimerRecorder metric) {
        return fromRangedTimer(metric);
    }

    private static void updateMetric(final RangedTimerRecorder metric, final StopwatchNotification notification) {
        metric.accept(notification.getDuration());
    }

    @Override
    boolean updateMetric(final RangedTimerRecorder metric, final MeasurementNotification notification) {
        final boolean success;
        if(success = notification instanceof StopwatchNotification)
            updateMetric(metric, (StopwatchNotification) notification);
        return success;
    }
}
