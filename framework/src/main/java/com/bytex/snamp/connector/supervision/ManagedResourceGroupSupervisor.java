package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.FrameworkService;

import java.util.Set;

/**
 * Represents supervisor of the managed resource group.
 * <p>
 *      Supervisor is used for resource discovery and elasticity management.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ManagedResourceGroupSupervisor extends FrameworkService {
    void discoverResources(final EntityMap<? extends ManagedResourceConfiguration> resources);

    /**
     * Gets immutable set of hosted resources.
     * @return Immutable set of hosted resources.
     */
    Set<String> getResources();
}
