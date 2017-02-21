package com.bytex.snamp.connector.health;

/**
 * Describes
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatusDetails {
    HealthStatus getStatus();

    /**
     * Gets root cause of the current status.
     * @return Root cause of the current status; or {@link NoRootCause} if status is {@link HealthStatus#OK}.
     * @see CausedByAttribute
     * @see ResourceUnavailable
     * @see NoRootCause
     */
    RootCause getRootCause();
}
