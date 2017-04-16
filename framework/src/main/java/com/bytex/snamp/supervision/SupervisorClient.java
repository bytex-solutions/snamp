package com.bytex.snamp.supervision;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.FrameworkServiceState;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import com.bytex.snamp.supervision.elasticity.ElasticityManager;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.concurrent.SpinWait.untilNull;

/**
 * Represents client for {@link Supervisor}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SupervisorClient extends ServiceHolder<Supervisor> implements Supervisor, SafeCloseable {
    private final BundleContext context;

    /**
     * Initializes a new supervisor client.
     *
     * @param context    The context of the bundle which holds this reference. Cannot be {@literal null}.
     * @param serviceRef The service reference to wrap. Cannot be {@literal null}.
     * @throws InstanceNotFoundException Service is no longer available from the specified reference.
     */
    public SupervisorClient(@Nonnull final BundleContext context, final ServiceReference<Supervisor> serviceRef) throws InstanceNotFoundException {
        super(context, serviceRef);
        this.context = context;
    }

    public static Optional<SupervisorClient> tryCreate(final BundleContext context,
                                     final String groupName,
                                     final Duration instanceTimeout) throws TimeoutException, InterruptedException {
        final ServiceReference<Supervisor> ref = untilNull(context, groupName, SupervisorClient::getSupervisorInstance, instanceTimeout);
        try {
            return Optional.of(new SupervisorClient(context, ref));
        } catch (InstanceNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Optional<SupervisorClient> tryCreate(final BundleContext context, final String groupName) {
        return Optional.ofNullable(getSupervisorInstance(context, groupName))
                .map(ref -> {
                    try {
                        return new SupervisorClient(context, ref);
                    } catch (final InstanceNotFoundException e) {
                        return null;
                    }
                });
    }

    /**
     * Constructs a new instance of the filter used to obtain service {@link Supervisor} from OSGi service registry.
     * @return A new instance of the filter builder.
     */
    public static SupervisorFilterBuilder filterBuilder(){
        final SupervisorFilterBuilder filter = new SupervisorFilterBuilder();
        filter.setServiceType(Supervisor.class);
        return filter;
    }

    private static ServiceReference<Supervisor> getSupervisorInstance(final BundleContext context,
                                                                final String groupName) {
        return filterBuilder().setGroupName(groupName).getServiceReference(context, Supervisor.class).orElse(null);
    }

    private static UnsupportedOperationException unsupportedServiceRequest(final String supervisorType,
                                                                           final Class<? extends SupportService> serviceType){
        return new UnsupportedOperationException(String.format("Supervisor %s doesn't expose %s service", supervisorType, serviceType));
    }

    private static String getSupervisorBundleHeader(final BundleContext context,
                                                 final String supervisorType,
                                                 final String header,
                                                 final Locale loc){
        final List<Bundle> candidates = SupervisorActivator.getSupervisorBundles(context, supervisorType);
        return candidates.isEmpty() ? null : candidates.get(0).getHeaders(loc != null ? loc.toString() : null).get(header);
    }

    public static  ConfigurationEntityDescription<SupervisorConfiguration> getConfigurationDescriptor(final BundleContext context, final String supervisorType) throws UnsupportedOperationException {
        ServiceReference<ConfigurationEntityDescriptionProvider> ref = null;
        try {
            ref = filterBuilder()
                    .setSupervisorType(supervisorType)
                    .setServiceType(ConfigurationEntityDescriptionProvider.class)
                    .getServiceReference(context, ConfigurationEntityDescriptionProvider.class)
                    .orElseThrow(() -> unsupportedServiceRequest(supervisorType, ConfigurationEntityDescriptionProvider.class));
            final ConfigurationEntityDescriptionProvider provider = context.getService(ref);
            return provider.getDescription(SupervisorConfiguration.class);
        } finally {
            if (ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets bundle state of the specified supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @return The state of the bundle.
     */
    public static int getState(final BundleContext context, final String supervisorType){
        final List<Bundle> bnds = SupervisorActivator.getSupervisorBundles(context, supervisorType);
        return bnds.isEmpty() ? Bundle.UNINSTALLED : bnds.get(0).getState();
    }

    /**
     * Gets localized display name of the supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @param loc The locale of the display name. May be {@literal null}.
     * @return The display name of the supervisor.
     */
    public static String getDisplayName(final BundleContext context, final String supervisorType, final Locale loc) {
        return getSupervisorBundleHeader(context, supervisorType, Constants.BUNDLE_NAME, loc);
    }

    /**
     * Gets version of the specified supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @return The version of the supervisor.
     */
    public static Version getVersion(final BundleContext context, final String supervisorType){
        return new Version(getSupervisorBundleHeader(context, supervisorType, Constants.BUNDLE_VERSION, null));
    }

    /**
     * Gets localized description of the supervisor.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param supervisorType Type of supervisor.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the supervisor.
     */
    public static String getDescription(final BundleContext context, final String supervisorType, final Locale loc) {
        return getSupervisorBundleHeader(context, supervisorType, Constants.BUNDLE_DESCRIPTION, loc);
    }

    /**
     * Gets name of the group served by this supervisor.
     * @return Name of the group.
     */
    public String getGroupName() {
        return SupervisorFilterBuilder.getGroupName(this);
    }

    /**
     * Gets state of this service.
     *
     * @return Service type.
     */
    @Override
    public FrameworkServiceState getState() {
        final Supervisor service = get();
        return service == null ? FrameworkServiceState.CLOSED : service.getState();
    }

    /**
     * Gets immutable set of group members.
     *
     * @return Immutable set of group members.
     */
    @Nonnull
    @Override
    public Set<String> getResources() {
        final Supervisor service = get();
        return service == null ? ImmutableSet.of() : service.getResources();
    }

    /**
     * Gets runtime configuration of this service.
     *
     * @return Runtime configuration of this service.
     * @implSpec Returning map is always immutable.
     */
    @Nonnull
    @Override
    public SupervisorInfo getConfiguration() {
        return get().getConfiguration();
    }

    @Override
    public void update(@Nonnull final SupervisorInfo configuration) throws Exception {
        get().update(configuration);
    }

    /**
     * Obtains supervisor service.
     *
     * @param objectType Type of supervisor service. Cannot be {@literal null}.
     * @return Supervisor service; or {@literal null} if service is not supported.
     * @see HealthStatusProvider
     * @see ElasticityManager
     */
    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        final Supervisor service = get();
        return service == null ? null : service.queryObject(objectType);
    }

    @Override
    public void close() {
        release(context);
    }
}
