package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of numeric type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface NumericGauge extends Metric {
    double getQuantile(final double quantile);
    double getDeviation();
    double getMeanValue(final MetricsInterval interval);
}
