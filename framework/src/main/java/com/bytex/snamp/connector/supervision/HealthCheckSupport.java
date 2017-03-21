package com.bytex.snamp.connector.supervision;

/**
 * Provides support for health checks.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthCheckSupport {
    /**
     * Determines whether the connected managed resource is alive.
     * @return Status of the remove managed resource.
     */
    HealthStatus getStatus();
}
