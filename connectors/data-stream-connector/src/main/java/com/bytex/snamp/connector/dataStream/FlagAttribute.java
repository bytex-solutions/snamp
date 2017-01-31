package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;
import com.bytex.snamp.connector.metrics.RatedFlagRecorder;
import com.bytex.snamp.instrumentation.measurements.BooleanMeasurement;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_FLAG_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedFlag;

/**
 * Represents attribute which exposes {@link com.bytex.snamp.connector.metrics.RatedFlag}.
 */
final class FlagAttribute extends MetricHolderAttribute<RatedFlagRecorder, ValueMeasurementNotification> {
    static final CompositeType TYPE = RATED_FLAG_TYPE;
    static final String NAME = "flag";
    private static final long serialVersionUID = -5234028741040752357L;

    FlagAttribute(final String name, final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(ValueMeasurementNotification.class, name, TYPE, descriptor, RatedFlagRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedFlagRecorder metric) {
        return fromRatedFlag(metric);
    }

    private static void updateMetric(final RatedFlagRecorder metric, final BooleanMeasurement measurement){
        metric.updateValue(measurement::getValue);
    }

    @Override
    void updateMetric(final RatedFlagRecorder metric, final ValueMeasurementNotification notification) {
        extractMeasurementAndUpdateMetric(notification, BooleanMeasurement.class, metric, FlagAttribute::updateMetric);
    }

    @Override
    protected boolean isNotificationEnabled(final ValueMeasurementNotification notification) {
        return representsMeasurement(notification) && notification.isMeasurement(BooleanMeasurement.class);
    }
}
