package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedGaugeFPRecorder;
import com.bytex.snamp.connector.notifications.measurement.InstantMeasurement;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

import static com.bytex.snamp.jmx.MetricsConverter.RATED_GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRatedGaugeFP;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GaugeFPAttribute extends MetricHolderAttribute<RatedGaugeFPRecorder, InstantMeasurement> {
    private static final long serialVersionUID = 4113436567386321873L;
    static final CompositeType TYPE = RATED_GAUGE_FP_TYPE;
    static final String NAME = "gaugeFP";

    GaugeFPAttribute(final String name, final AttributeDescriptor descriptor) {
        super(InstantMeasurement.class, name, TYPE, descriptor, RatedGaugeFPRecorder::new);
    }

    @Override
    CompositeData getValue(final RatedGaugeFPRecorder metric) {
        return fromRatedGaugeFP(metric);
    }


    @Override
    void updateMetric(RatedGaugeFPRecorder metric, InstantMeasurement notification) {
        if (notification.isFloatingPoint())
            metric.updateValue(x -> notification.applyAsDouble(x).orElse(x));
    }
}
