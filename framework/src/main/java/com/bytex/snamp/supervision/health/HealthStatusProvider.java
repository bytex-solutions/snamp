package com.bytex.snamp.supervision.health;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.supervision.SupervisorAggregatedService;

import javax.annotation.Nonnull;

/**
 * Represents health check service used to supervise groups of managed resources.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface HealthStatusProvider extends SupervisorAggregatedService, Stateful {
    /**
     * Gets status of the managed resource group.
     * @return Gets status of the managed resource group.
     */
    @Nonnull
    ResourceGroupHealthStatus getStatus();

}
