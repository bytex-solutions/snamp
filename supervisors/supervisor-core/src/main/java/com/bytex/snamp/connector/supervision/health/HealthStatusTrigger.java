package com.bytex.snamp.connector.supervision.health;

import com.bytex.snamp.connector.supervision.HealthStatus;

/**
 * Represents trigger used to intercept health status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface HealthStatusTrigger {
    HealthStatusTrigger IDENTITY = (p, n) -> n;

    HealthStatus statusChanged(final HealthStatus previousStatus, final HealthStatus newStatus);
}
