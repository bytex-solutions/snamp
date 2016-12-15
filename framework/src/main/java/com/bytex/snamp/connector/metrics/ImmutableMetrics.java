package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.ResettableIterator;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents immutable implementation of {@link MetricsSupport}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
@Immutable
public class ImmutableMetrics implements MetricsSupport {
    private final ImmutableMap<String, Metric> metrics;

    private ImmutableMetrics(final Iterator<? extends Metric> metrics){
        final ImmutableMap.Builder<String, Metric> metricBuilder = ImmutableMap.builder();
        while (metrics.hasNext()) {
            final Metric m = metrics.next();
            metricBuilder.put(m.getName(), m);
        }
        this.metrics = metricBuilder.build();
    }

    public ImmutableMetrics(final Metric... metrics) {
        this(ResettableIterator.of(metrics));
    }

    public <I> ImmutableMetrics(final I[] metrics, final Function<? super I, ? extends Metric> converter){
        this(Arrays.stream(metrics).map(converter).iterator());
    }

    private <M extends Metric> Iterable<M> getMetrics(final Collection<? extends Metric> metrics, final Class<M> metricType) {
        return () -> metrics.stream()
                .filter(metricType::isInstance)
                .map(metricType::cast)
                .iterator();
    }

    /**
     * Returns a set of supported metrics.
     *
     * @param metricType Type of the metrics.
     * @return Immutable set of metrics.
     */
    @Override
    public final  <M extends Metric> Iterable<? extends M> getMetrics(final Class<M> metricType) {
        return getMetrics(metrics.values(), metricType);
    }

    /**
     * Gets metric by its name.
     *
     * @param metricName Name of the metric.
     * @return An instance of metric; or {@literal null}, if metrics doesn't exist.
     */
    @Override
    public final Metric getMetric(final String metricName) {
        return metrics.get(metricName);
    }

    /**
     * Resets all metrics.
     */
    @Override
    public final void resetAll() {
        metrics.values().forEach(Metric::reset);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public final Iterator<Metric> iterator() {
        return metrics.values().iterator();
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing class,
     * actions are performed in the order of iteration (if an iteration order
     * is specified).  Exceptions thrown by the action are relayed to the
     * caller.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    public final void forEach(final Consumer<? super Metric> action) {
        metrics.values().forEach(action);
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @Override
    public final Spliterator<Metric> spliterator() {
        return metrics.values().spliterator();
    }
}
