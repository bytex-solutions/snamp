package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.AbstractMetric;
import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.instrumentation.measurements.jmx.MeasurementNotification;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a holder for metric.
 * @param <M> Type of metric recorder
 * @param <N> Type of notifications that can be handled by this attribute
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class MetricHolderAttribute<M extends AbstractMetric, N extends Notification> extends DistributedAttribute<CompositeData, N> implements AutoCloseable, Stateful {
    private static final long serialVersionUID = 2645456225474793148L;
    private volatile M metric;
    private final Predicate<? super Serializable> isInstance;

    MetricHolderAttribute(final Class<N> notificationType,
                          final String name,
                          final CompositeType type,
                          final AttributeDescriptor descriptor,
                          final Function<? super String, ? extends M> metricFactory) throws InvalidSyntaxException {
        super(notificationType, name, type, type.getDescription(), descriptor);
        metric = metricFactory.apply(name);
        assert metric != null;
        isInstance = metric.getClass()::isInstance;
    }

    private void resetImpl(){
        metric.reset();
    }

    @Override
    public final void reset() {
        metric.reset();
    }

    abstract CompositeData getValue(final M metric);

    private CompositeData getValueImpl(){
        return getValue(metric);
    }

    @Override
    protected final CompositeData getValue() {
        return getValue(metric);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final M takeSnapshot() {
        final AbstractMetric result = metric.clone();
        assert isInstance.test(result);
        return (M) result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final void loadFromSnapshot(final Serializable snapshot) {
        if(isInstance.test(snapshot))
            metric = (M) snapshot;
    }

    abstract void updateMetric(final M metric, final N notification);

    @Override
    protected final CompositeData changeAttributeValue(final N notification) {
        final M metric = this.metric;
        updateMetric(metric, notification);
        return getValue(metric);
    }

    @Override
    public final void close() {
        metric = null;
    }

    static <M extends Measurement, S extends AbstractMetric> void extractMeasurementAndUpdateMetric(final MeasurementNotification<?> notification,
                                                                                                       final Class<M> measurementType,
                                                                                                       final S metric,
                                                                                                       final BiConsumer<? super S, ? super M> measurementHandler) {
        if (measurementType.isInstance(notification.getMeasurement()))
            measurementHandler.accept(metric, measurementType.cast(notification.getMeasurement()));
    }
}
