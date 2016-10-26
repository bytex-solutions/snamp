package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a holder for metric.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MetricHolderAttribute<M extends Metric & Serializable> extends MessageDrivenAttribute<CompositeData> {
    private static final long serialVersionUID = 2645456225474793148L;
    private volatile M metric;
    private final Predicate<Object> isInstance;

    MetricHolderAttribute(final String name,
                          final CompositeType type,
                          final String description,
                          final AttributeDescriptor descriptor,
                          final Function<? super String, ? extends M> metricFactory) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
        metric = metricFactory.apply(name);
        assert metric != null;
        isInstance = metric.getClass()::isInstance;
    }

    abstract CompositeData getValue(final M metric);

    @Override
    protected final CompositeData getValue() {
        return getValue(metric);
    }

    @Override
    protected final M takeSnapshot() {
        return metric;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final void loadFromSnapshot(final Serializable snapshot) {
        if(isInstance.test(snapshot))
            metric = (M) snapshot;
    }

    abstract boolean updateMetric(final M metric, final MeasurementNotification notification);

    @Override
    protected final boolean accept(final MeasurementNotification notification) {
        return updateMetric(metric, notification);
    }
}
