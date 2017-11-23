package com.bytex.snamp.connector.metrics;

/**
 * Provides statistical information about notifications.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface NotificationMetrics extends Metric {
    /**
     * Gets rate of all emitted notifications.
     * @return Rate of all emitted notifications.
     */
    Rate notifications();

    @Override
    NotificationMetrics clone();
}
