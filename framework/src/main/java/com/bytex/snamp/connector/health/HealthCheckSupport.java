package com.bytex.snamp.connector.health;

import com.bytex.snamp.health.HealthStatus;

import java.time.Duration;

/**
 * Provides support for health checks.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthCheckSupport {
    /**
     * Determines whether the connected managed resource is alive.
     * @param timeout Timeout required to identify health status.
     * @return Status of the remove managed resource.
     */
    HealthStatus getStatus(final Duration timeout);
}
