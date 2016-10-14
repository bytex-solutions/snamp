package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.Gauge64Recorder;
import com.bytex.snamp.connector.metrics.RatedGauge64;
import com.bytex.snamp.connector.metrics.RatedGauge64Recorder;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;

import javax.management.openmbean.CompositeType;
import java.util.OptionalLong;
import java.util.function.LongFunction;

import static com.bytex.snamp.jmx.MetricsConverter.*;

/**
 * Holds {@link com.bytex.snamp.connector.metrics.RatedGauge64} as attribute.
 * @since 2.0
 * @version 2.0
 */
final class Gauge64Attribute extends MetricHolderAttribute<RatedGauge64> {
    private static final CompositeType TYPE = RATED_GAUGE_64_TYPE;
    private static final String DESCRIPTION = "Represents 64-bit gauge";
    private static final long serialVersionUID = -5234028741040752357L;
    private final RatedGauge64Recorder recorder;

    Gauge64Attribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE, DESCRIPTION, descriptor);
        recorder = new RatedGauge64Recorder(name);
    }

    private static void updateGauge(final LongFunction<OptionalLong> newValueProvider, final Gauge64Recorder recorder) {
        recorder.updateValue(value -> {
            final OptionalLong result = newValueProvider.apply(value);
            assert result.isPresent();
            return result.getAsLong();
        });
    }

    private boolean accept(final ValueChangedNotification notification) {
        final boolean result;
        if (result = notification.isInteger()) {
            updateGauge(notification, recorder);
        }
        return result;
    }

    @Override
    boolean accept(final MeasurementNotification notification) {
        return notification instanceof ValueChangedNotification && accept((ValueChangedNotification)notification);
    }
}
