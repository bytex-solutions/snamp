package com.bytex.snamp.connector.metrics.collect;

import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.notifications.measurement.MeasurementNotification;
import com.bytex.snamp.connector.notifications.measurement.ValueChangedNotification;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.concurrent.ExecutorService;
import java.util.function.*;

/**
 * Provides handling of messages and converts it to the metrics.
 * @since 2.0
 * @version 2.0
 */
public class MetricsCollector implements Consumer<MeasurementNotification>, BiConsumer<MeasurementNotification, ExecutorService> {

    private static final class FilteredMetric implements Metric, Predicate<MeasurementNotification>, Consumer<MeasurementNotification>{
        private final Predicate<? super MeasurementNotification> filter;
        private final Metric metric;

        private FilteredMetric(final Metric m, final Predicate<? super MeasurementNotification> filter){
            this.metric = Objects.requireNonNull(m);
            this.filter = filter == null ? n -> true : filter;
        }

        @Override
        public String getName() {
            return metric.getName();
        }

        @Override
        public void reset() {
            metric.reset();
        }

        @Override
        public boolean test(final MeasurementNotification notification) {
            return filter.test(notification);
        }

        @Override
        public void accept(final MeasurementNotification notification) {

        }
    }

    private final KeyedObjects<String, FilteredMetric> metrics = AbstractKeyedObjects.create(FilteredMetric::getName);
    private boolean autoCreate = false;

    public boolean isAutoCreateMetrics(){
        return autoCreate;
    }

    public void setAutoCreateMetrics(final boolean value){
        autoCreate = value;
    }

    private boolean addMetric(final FilteredMetric metric){
        return metrics.putIfAbsent(metric);
    }

    public boolean addMetric(final Metric m, final Predicate<? super MeasurementNotification> filter) {
        return addMetric(new FilteredMetric(m, filter));
    }

    public boolean addMetric(final String name, final Function<? super String, ? extends Metric> factory, final Predicate<? super MeasurementNotification> filter) {
        return !metrics.containsKey(name) && addMetric(new FilteredMetric(factory.apply(name), filter));
    }

    public boolean removeMetric(final String name){
        return metrics.remove(name) != null;
    }

    public Metric getMetric(final String name){
        return metrics.get(name);
    }

    public <M extends Metric> OptionalLong tryGetMetric(final String name, final Class<M> metricType, final ToLongFunction<? super M> handler) {
        final Metric m = metrics.get(name);
        return metricType.isInstance(m) ? OptionalLong.of(handler.applyAsLong(metricType.cast(m))) : OptionalLong.empty();
    }

    public <M extends Metric> OptionalDouble tryGetMetric(final String name, final Class<M> metricType, final ToDoubleFunction<? super M> handler){
        final Metric m = metrics.get(name);
        return metricType.isInstance(m) ? OptionalDouble.of(handler.applyAsDouble(metricType.cast(m))) : OptionalDouble.empty();
    }

    public <M extends Metric, V> Optional<V> tryGetMetric(final String name, final Class<M> metricType, final Function<? super M, ? extends V> handler){
        final Metric m = metrics.get(name);
        return metricType.isInstance(m) ? Optional.of(handler.apply(metricType.cast(m))) : Optional.empty();
    }

    private void handleNotification(final MeasurementNotification notification){

    }

    private void accept(final ValueChangedNotification notification, final ExecutorService threadPool){

    }

    @Override
    public void accept(final MeasurementNotification notification) {
        if(notification instanceof ValueChangedNotification)
            accept(notification);
    }

    @Override
    public void accept(final MeasurementNotification notification, final ExecutorService threadPool) {

    }
}
