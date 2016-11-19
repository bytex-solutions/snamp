package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.md.notifications.StringMeasurementNotification;
import com.bytex.snamp.connector.metrics.RatedStringGaugeRecorder;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_STRING_GAUGE_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedStringGauge;

/**
 * Represents attribute which can collect statistical information about receiving string values.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class StringGaugeAttribute extends MetricHolderAttribute<RatedStringGaugeRecorder, StringMeasurementNotification> {
    static final CompositeType TYPE = RATED_STRING_GAUGE_TYPE;
    static final String NAME = "stringGauge";
    private static final long serialVersionUID = -5234028741040752357L;

    StringGaugeAttribute(final String name, final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(StringMeasurementNotification.class, name, TYPE, descriptor, RatedStringGaugeRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedStringGaugeRecorder metric) {
        return fromRatedStringGauge(metric);
    }

    @Override
    void updateMetric(final RatedStringGaugeRecorder metric, final StringMeasurementNotification notification) {
        metric.updateValue(notification);
    }
}
