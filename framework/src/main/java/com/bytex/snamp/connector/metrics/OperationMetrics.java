package com.bytex.snamp.connector.metrics;

/**
 * Provides statistical information about invocation of operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface OperationMetrics extends Metrics {
    long getNumberOfInvocations();

    long getNumberOfInvocations(final MetricsInterval interval);
}
