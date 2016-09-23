package com.bytex.snamp.connector.metrics.collect;

import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableMap;

import java.util.EventListener;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Represents a handled for typed metric.
 */
abstract class AbstractMetricHandler<M extends Metric> implements MetricHandler {
    protected interface NotificationProcessor<M extends Metric, N extends MeasurementNotification> extends EventListener{
        void handle(final M metric, final N notification);
    }

    private final Predicate<? super MeasurementNotification> filter;
    private final ImmutableMap<Class<? extends MeasurementNotification>, NotificationProcessor> processors;
    final M metric;

    AbstractMetricHandler(final M metric, final Predicate<? super MeasurementNotification> filter){
        this.metric = Objects.requireNonNull(metric);
        this.filter = filter == null ? n -> true : filter;
    }

    @Override
    public final boolean test(final MeasurementNotification notification) {
        return processors.containsKey(notification.getClass()) && filter.test(notification);
    }

    @Override
    public final String getName() {
        return metric.getName();
    }

    @Override
    public final void reset() {
        metric.reset();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void accept(final MeasurementNotification notification) {
        final NotificationProcessor processor = processors.get(notification.getClass());
        if (processor != null)
            processor.handle(metric, notification);
    }
}
