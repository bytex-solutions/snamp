package com.bytex.snamp.connector.metrics;

/**
 * Represents statistics about some metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface Statistic extends Metric {
    double getQuantile(final double quantile);
    double getDeviation();
}
