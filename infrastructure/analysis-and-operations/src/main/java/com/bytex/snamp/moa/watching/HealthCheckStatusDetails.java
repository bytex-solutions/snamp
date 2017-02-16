package com.bytex.snamp.moa.watching;

import com.bytex.snamp.connector.health.HealthCheckStatus;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthCheckStatusDetails {
    /**
     * Gets health check status.
     * @return Health check status.
     */
    HealthCheckStatus getStatus();

    /**
     * Gets the most problematic resource.
     * @return The most problematic resource. Can be empty if status is {@link HealthCheckStatus#OK}.
     */
    String getResourceName();

    /**
     * Gets root cause of the current status.
     * @return Root cause of the current status; or {@literal null} status is {@link HealthCheckStatus#OK}.
     * @see CausedByAttribute
     * @see ResourceUnavailable
     */
    RootCause getRootCause();
}
