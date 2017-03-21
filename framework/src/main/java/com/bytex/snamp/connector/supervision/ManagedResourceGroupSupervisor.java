package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.core.FrameworkService;

import java.util.Set;

/**
 * Represents supervisor of the managed resource group.
 * <p>
 *      Supervisor is used for resource discovery, health checks and elasticity management.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ManagedResourceGroupSupervisor extends FrameworkService, HealthCheckSupport {
    /**
     * Gets immutable set of group members.
     * @return Immutable set of group members.
     */
    Set<String> getResources();
}
