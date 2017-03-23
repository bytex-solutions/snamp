package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.connector.ManagedResourceAggregatedService;

/**
 * Provides statistical information about managed resource connector.
 * <p>
 *     This viewer can be obtained with method {@link com.bytex.snamp.connector.ManagedResourceConnector#queryObject(Class)}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface MetricsSupport extends Iterable<Metric>, ManagedResourceAggregatedService {
    /**
     * Returns a set of supported metrics.
     * @param metricType Type of the metrics.
     * @return Immutable set of metrics.
     */
    <M extends Metric> Iterable<? extends M> getMetrics(final Class<M> metricType);

    /**
     * Gets metric by its name.
     * @param metricName Name of the metric.
     * @return An instance of metric; or {@literal null}, if metrics doesn't exist.
     */
    Metric getMetric(final String metricName);

    /**
     * Resets all metrics.
     */
    default void resetAll(){
        forEach(Metric::reset);
    }
}
