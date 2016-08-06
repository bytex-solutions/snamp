package com.bytex.snamp.connectors.metrics;

/**
 * Provides statistical information about attributes.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface AttributeMetrics extends Metrics {
    /**
     * Gets total number of reads for all attributes.
     * @return A number of reads for all attributes.
     */
    long getNumberOfReads();

    /**
     * Gets number of reads for all attributes for the last time.
     * @param interval Interval of time.
     * @return A number of reads for all attributes.
     */
    long getNumberOfReads(final MetricsInterval interval);

    /**
     * Gets total number of writes for all attributes.
     * @return A number of writes for all attributes.
     */
    long getNumberOfWrites();

    /**
     * Gets total number of writes for all attributes for the last time.
     * @param interval Interval of time.
     * @return A number of writes for all attributes.
     */
    long getNumberOfWrites(final MetricsInterval interval);
}
