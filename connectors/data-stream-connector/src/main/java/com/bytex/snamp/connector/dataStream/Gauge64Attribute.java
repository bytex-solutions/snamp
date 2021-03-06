package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedGauge64Recorder;
import com.bytex.snamp.instrumentation.measurements.IntegerMeasurement;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_GAUGE_64_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGauge64;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RatedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class Gauge64Attribute extends MetricHolderAttribute<RatedGauge64Recorder, ValueMeasurementNotification> {
    static final CompositeType TYPE = RATED_GAUGE_64_TYPE;
    static final String NAME = "gauge64";
    private static final long serialVersionUID = -5234028741040752357L;

    Gauge64Attribute(final String name, final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(ValueMeasurementNotification.class, name, TYPE, descriptor, RatedGauge64Recorder::new);
    }

    @Override
    CompositeData getValue(final RatedGauge64Recorder metric) {
        return fromRatedGauge64(metric);
    }

    private static void updateMetric(final RatedGauge64Recorder metric, final IntegerMeasurement measurement){
        metric.updateValue(measurement::getValue);
    }

    @Override
    void updateMetric(final RatedGauge64Recorder metric, final ValueMeasurementNotification notification) {
        extractMeasurementAndUpdateMetric(notification, IntegerMeasurement.class, metric, Gauge64Attribute::updateMetric);
    }

    @Override
    protected boolean isNotificationEnabled(final ValueMeasurementNotification notification) {
        return representsMeasurement(notification) && notification.isMeasurement(IntegerMeasurement.class);
    }
}
