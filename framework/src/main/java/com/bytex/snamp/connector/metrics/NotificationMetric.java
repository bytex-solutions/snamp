package com.bytex.snamp.connector.metrics;

/**
 * Provides statistical information about notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface NotificationMetric extends Metric {
    /**
     * Gets total number of all emitted notifications.
     * @return A number of all emitted notifications.
     */
    long getTotalNumberOfNotifications();

    long getLastNumberOfEmitted(final MetricsInterval interval);
}
