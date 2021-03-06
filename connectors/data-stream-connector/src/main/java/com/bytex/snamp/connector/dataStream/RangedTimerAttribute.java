package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RangedTimerRecorder;
import com.bytex.snamp.instrumentation.measurements.jmx.TimeMeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

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
final class RangedTimerAttribute extends MetricHolderAttribute<RangedTimerRecorder, TimeMeasurementNotification> {
    static final CompositeType TYPE = RANGED_TIMER_TYPE;
    static final String NAME = "rangedTimer";
    private static final long serialVersionUID = -5234028741040752357L;

    private RangedTimerAttribute(final String name, final AttributeDescriptor descriptor, final Duration rangeStart, final Duration rangeEnd) throws InvalidSyntaxException {
        super(TimeMeasurementNotification.class, name, TYPE, descriptor, (n) -> new RangedTimerRecorder(n, rangeStart, rangeEnd));
    }

    RangedTimerAttribute(final String name, final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException, InvalidSyntaxException {
        this(name,
                descriptor,
                DataStreamConnectorConfigurationDescriptionProvider.parseRangeStartAsDuration(descriptor),
                DataStreamConnectorConfigurationDescriptionProvider.parseRangeEndAsDuration(descriptor));
    }

    @Override
    CompositeData getValue(final RangedTimerRecorder metric) {
        return fromRangedTimer(metric);
    }

    @Override
    void updateMetric(final RangedTimerRecorder metric, final TimeMeasurementNotification notification) {
        metric.accept(notification.getMeasurement().convertTo(Duration.class));
    }

    @Override
    protected boolean isNotificationEnabled(final TimeMeasurementNotification notification) {
        return representsMeasurement(notification);
    }
}
