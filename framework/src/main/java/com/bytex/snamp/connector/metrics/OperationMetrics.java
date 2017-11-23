package com.bytex.snamp.connector.metrics;

/**
 * Provides statistical information about invocation of operations.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface OperationMetrics extends Metric {
    /**
     * Gets rate of all invocations.
     * @return Rate of all invocations.
     */
    Rate invocations();

    @Override
    OperationMetrics clone();
}
