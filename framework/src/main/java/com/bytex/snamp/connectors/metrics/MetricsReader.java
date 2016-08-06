package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.Aggregator;

import javax.management.MBeanFeatureInfo;

/**
 * Provides statistical information about managed resource connector.
 * <p>
 *     This viewer can be obtained with method {@link com.bytex.snamp.connectors.ManagedResourceConnector#queryObject(Class)}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface MetricsReader extends Aggregator {
    /**
     * Gets metrics for the specified resource feature.
     * @param featureType Type of the feature.
     * @return A set of metrics.
     * @see AttributeMetrics
     * @see OperationMetrics
     * @see NotificationMetrics
     */
    Metrics getMetrics(final Class<? extends MBeanFeatureInfo> featureType);

    /**
     * Resets all metrics.
     */
    void resetAll();
}
