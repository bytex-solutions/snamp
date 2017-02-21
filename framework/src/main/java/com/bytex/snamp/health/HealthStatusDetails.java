package com.bytex.snamp.health;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatusDetails {
    /**
     * Gets health check status.
     * @return Health check status.
     */
    HealthStatus getStatus();

    /**
     * Gets the most problematic resource.
     * @return The most problematic resource. Can be empty if status is {@link HealthStatus#OK}.
     */
    String getResourceName();

    /**
     * Gets root cause of the current status.
     * @return Root cause of the current status; or {@link NoRootCause} if status is {@link HealthStatus#OK}.
     * @see CausedByAttribute
     * @see ResourceUnavailable
     * @see NoRootCause
     */
    RootCause getRootCause();
}
