package com.bytex.snamp.supervision;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.health.HealthCheckSupport;

/**
 * Represents health check service used to supervise groups of managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatusProvider extends SupervisorService, Stateful, HealthCheckSupport {
    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    void addHealthStatusEventListener(final HealthStatusEventListener listener);

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    void removeHealthStatusEventListener(final HealthStatusEventListener listener);
}
