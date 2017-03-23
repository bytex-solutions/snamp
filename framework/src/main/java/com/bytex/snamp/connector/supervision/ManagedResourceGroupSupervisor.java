package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.core.FrameworkService;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Represents supervisor of the managed resource group.
 * <p>
 *      Supervisor is used for resource discovery, health checks and elasticity management.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ManagedResourceGroupSupervisor extends FrameworkService {
    /**
     * Gets immutable set of group members.
     * @return Immutable set of group members.
     */
    Set<String> getResources();

    /**
     * Obtains supervisor service.
     * @param objectType Type of supervisor service. Cannot be {@literal null}.
     * @param <T> Type of supervisor service.
     * @return Supervisor service; or {@literal null} if service is not supported.
     * @see HealthStatusProvider
     * @see ElasticityManager
     */
    @Override
    <T> T queryObject(@Nonnull final Class<T> objectType);
}
