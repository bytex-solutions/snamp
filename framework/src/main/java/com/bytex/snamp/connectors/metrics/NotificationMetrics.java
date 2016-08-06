package com.bytex.snamp.connectors.metrics;

/**
 * Provides statistical information about notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface NotificationMetrics extends Metrics {
    /**
     * Gets total number of all emitted notifications.
     * @return A number of all emitted notifications.
     */
    long getNumberOfEmitted();

    long getNumberOfEmitted(final MetricsInterval interval);
}
