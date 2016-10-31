package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedFlagRecorder;
import com.bytex.snamp.connector.metrics.RatedStringGaugeRecorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.*;

/**
 * Represents attribute which can collect statistical information about receiving string values.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class StringGaugeAttribute extends MetricHolderAttribute<RatedStringGaugeRecorder> {
    static final CompositeType TYPE = RATED_STRING_GAUGE_TYPE;
    static final String NAME = "stringGauge";
    private static final String DESCRIPTION = "Represents statistical data about input stream of strings";
    private static final long serialVersionUID = -5234028741040752357L;

    StringGaugeAttribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE, DESCRIPTION, descriptor, RatedStringGaugeRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedStringGaugeRecorder metric) {
        return fromRatedStringGauge(metric);
    }

    private static boolean updateMetric(final RatedStringGaugeRecorder metric, final ValueChangedNotification notification) {
        final boolean success;
        if (success = notification.isString())
            metric.updateValue(current -> notification.applyAsString(current).orElse(current));
        return success;
    }

    @Override
    boolean updateMetric(final RatedStringGaugeRecorder metric, final MeasurementNotification notification) {
        return notification instanceof ValueChangedNotification && updateMetric(metric, (ValueChangedNotification) notification);
    }
}
