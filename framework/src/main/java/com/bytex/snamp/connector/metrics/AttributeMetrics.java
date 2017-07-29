package com.bytex.snamp.connector.metrics;

/**
 * Provides statistical information about attributes.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface AttributeMetrics extends Metric {
    /**
     * Gets rate of attribute writes.
     * @return Rate of attribute writes.
     */
    Rate writes();

    /**
     * Gets rate of attribute reads.
     * @return Rate of attribute reads.
     */
    Rate reads();

    @Override
    AttributeMetrics clone();
}
