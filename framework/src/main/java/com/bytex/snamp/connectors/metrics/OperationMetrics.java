package com.bytex.snamp.connectors.metrics;

/**
 * Provides statistical information about invocation of operations.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface OperationMetrics extends Metrics {
    long getNumberOfInvocations();

    long getNumberOfInvocations(final MetricsInterval interval);
}
