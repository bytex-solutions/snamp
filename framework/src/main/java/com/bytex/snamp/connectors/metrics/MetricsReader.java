package com.bytex.snamp.connectors.metrics;

import javax.management.MBeanFeatureInfo;

/**
 * Provides statistical information about managed resource connector.
 * <p>
 *     This viewer can be obtained with method {@link com.bytex.snamp.connectors.ManagedResourceConnector#queryObject(Class)}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface MetricsReader {
    /**
     * Gets metrics for the specified resource feature.
     * @param featureType Type of the feature.
     * @return A set of metrics.
     * @see AttributeMetrics
     */
    Metrics getMetrics(final Class<? extends MBeanFeatureInfo> featureType);

    /**
     * Resets all metrics.
     */
    void resetAll();
}
