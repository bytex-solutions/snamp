package com.bytex.snamp.supervision.health;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.supervision.SupervisorService;

import javax.annotation.Nonnull;

/**
 * Represents health check service used to supervise groups of managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatusProvider extends SupervisorService, Stateful {
    /**
     * Gets status of the managed resource group.
     * @return Gets status of the managed resource group.
     */
    @Nonnull
    ResourceGroupHealthStatus getStatus();

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     * @param handback Handback object that will be returned into listener.
     */
    void addHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener, final Object handback);

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    void addHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener);

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    void removeHealthStatusEventListener(@Nonnull final HealthStatusEventListener listener);
}
