package com.bytex.snamp.connector.health;

import com.bytex.snamp.connector.ManagedResourceAggregatedService;

import javax.annotation.Nonnull;

/**
 * Provides support for health checks.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthCheckSupport extends ManagedResourceAggregatedService {
    /**
     * Determines whether the connected managed resource is alive.
     * @return Status of the remove managed resource.
     */
    @Nonnull
    HealthStatus getStatus();
}
