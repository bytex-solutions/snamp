package com.bytex.snamp.connector.metrics;

/**
 * Represents gauge of numeric type.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface NumericGauge extends Metric {
    double getDeviation();
    double getQuantile(final float quantile);
    double getMeanValue(final MetricsInterval interval);

    @Override
    NumericGauge clone();
}
