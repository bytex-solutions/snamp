package com.bytex.snamp.connector.metrics;

/**
 * Provides statistical information about attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface AttributeMetric extends Metric {
    /**
     * Gets total number of reads for all attributes.
     * @return A number of reads for all attributes.
     */
    long getTotalNumberOfReads();

    /**
     * Gets number of reads for all attributes for the last time.
     * @param interval Interval of time.
     * @return A number of reads for all attributes.
     */
    long getLastNumberOfReads(final MetricsInterval interval);

    /**
     * Gets total number of writes for all attributes.
     * @return A number of writes for all attributes.
     */
    long getTotalNumberOfWrites();

    /**
     * Gets total number of writes for all attributes for the last time.
     * @param interval Interval of time.
     * @return A number of writes for all attributes.
     */
    long getLastNumberOfWrites(final MetricsInterval interval);
}
