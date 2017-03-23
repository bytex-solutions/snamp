package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.core.FrameworkService;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

/**
 * Represents supervisor of the managed resource group.
 * <p>
 *      Supervisor is used for resource discovery, health checks and elasticity management.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ManagedResourceGroupSupervisor extends FrameworkService, AutoCloseable {
    /**
     * This namespace must be defined in Provide-Capability manifest header inside of the bundle containing implementation
     * of Managed Resource Group Supervisor.
     * <p>
     *     Example: Provide-Capability: com.bytex.snamp.supervisor; type=openstack
     */
    String CAPABILITY_NAMESPACE = "com.bytex.snamp.supervisor";

    /**
     * This property must be defined in Provide-Capability manifest header and specify type of Managed Resource Group Supervisor.
     * @see #CAPABILITY_NAMESPACE
     */
    String TYPE_CAPABILITY_ATTRIBUTE = "type";

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

    static String getSupervisorType(final Bundle bnd) {
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElse("");
    }
}
