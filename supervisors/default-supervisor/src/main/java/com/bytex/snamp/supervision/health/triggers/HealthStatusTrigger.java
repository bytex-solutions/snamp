package com.bytex.snamp.supervision.health.triggers;

import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

/**
 * Represents a trigger that is invoked by supervisor when health status of the group was changed.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface HealthStatusTrigger {
    HealthStatusTrigger NO_OP = (p, n) -> {};
    void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus);
}
