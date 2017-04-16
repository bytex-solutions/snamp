package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedGaugeFPRecorder;
import com.bytex.snamp.instrumentation.measurements.FloatingPointMeasurement;
import com.bytex.snamp.instrumentation.measurements.jmx.ValueMeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGaugeFP;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GaugeFPAttribute extends MetricHolderAttribute<RatedGaugeFPRecorder, ValueMeasurementNotification> {
    private static final long serialVersionUID = 4113436567386321873L;
    static final CompositeType TYPE = RATED_GAUGE_FP_TYPE;
    static final String NAME = "gaugeFP";

    GaugeFPAttribute(final String name, final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(ValueMeasurementNotification.class, name, TYPE, descriptor, RatedGaugeFPRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedGaugeFPRecorder metric) {
        return fromRatedGaugeFP(metric);
    }

    private static void updateMetric(final RatedGaugeFPRecorder metric, final FloatingPointMeasurement measurement){
        metric.updateValue(measurement::getValue);
    }

    @Override
    void updateMetric(final RatedGaugeFPRecorder metric, final ValueMeasurementNotification notification) {
        extractMeasurementAndUpdateMetric(notification, FloatingPointMeasurement.class, metric, GaugeFPAttribute::updateMetric);
    }

    @Override
    protected boolean isNotificationEnabled(final ValueMeasurementNotification notification) {
        return representsMeasurement(notification) && notification.isMeasurement(FloatingPointMeasurement.class);
    }
}
