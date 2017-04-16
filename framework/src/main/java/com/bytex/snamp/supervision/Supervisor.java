package com.bytex.snamp.supervision;

import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.core.StatefulFrameworkService;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents supervisor of the managed resource group.
 * <p>
 *      Supervisor is used for resource discovery, health checks and elasticity management.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface Supervisor extends StatefulFrameworkService, Closeable {
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

    SupervisorInfo EMPTY_CONFIGURATION = new EmptySupervisorInfo();

    /**
     * Gets immutable set of group members.
     * @return Immutable set of group members.
     */
    @Nonnull
    Set<String> getResources();

    /**
     * Gets runtime configuration of this service.
     *
     * @return Runtime configuration of this service.
     * @implSpec Returning map is always immutable.
     */
    @Nonnull
    @Override
    SupervisorInfo getConfiguration();



    void update(@Nonnull final SupervisorInfo configuration) throws Exception;

    /**
     * Obtains supervisor service.
     * @param objectType Type of supervisor service. Cannot be {@literal null}.
     * @param <T> Type of supervisor service.
     * @return Supervisor service; or {@literal null} if service is not supported.
     * @see HealthStatusProvider
     * @see ElasticityManager
     */
    @Override
    <T> Optional<T> queryObject(@Nonnull final Class<T> objectType);

    static String getSupervisorType(final Bundle bnd) {
        final BundleRevision revision = bnd.adapt(BundleRevision.class);
        assert revision != null;
        return revision.getCapabilities(CAPABILITY_NAMESPACE)
                .stream()
                .map(capability -> capability.getAttributes().get(TYPE_CAPABILITY_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElse("")
                .intern();
    }

    static String getSupervisorType(final Class<? extends Supervisor> supervisorType) {
        final BundleContext context = Utils.getBundleContext(supervisorType);
        assert context != null;
        return getSupervisorType(context.getBundle());
    }

    static boolean isSupervisorBundle(final Bundle bnd) {
        return bnd != null && !isNullOrEmpty(getSupervisorType(bnd));
    }
}
