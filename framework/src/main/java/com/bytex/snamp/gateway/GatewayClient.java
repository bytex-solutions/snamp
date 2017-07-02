package com.bytex.snamp.gateway;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.core.FrameworkServiceState;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanFeatureInfo;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.concurrent.SpinWait.untilNull;

/**
 * Represents a client of resource connector that can be used by gateway consumers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GatewayClient extends ServiceHolder<Gateway> implements Gateway {
    private final BundleContext context;

    /**
     * Initializes a new gateway client.
     *
     * @param context    The context of the bundle which holds this reference. Cannot be {@literal null}.
     * @param serviceRef The service reference to wrap. Cannot be {@literal null}.
     * @throws InstanceNotFoundException Gateway instance is no longer available from the specified reference.
     */
    public GatewayClient(@Nonnull final BundleContext context, final ServiceReference<Gateway> serviceRef) throws InstanceNotFoundException {
        super(context, serviceRef);
        this.context = context;
    }

    public static Optional<GatewayClient> tryCreate(@Nonnull final BundleContext context,
                                     final String instanceName,
                                     final Duration instanceTimeout) throws TimeoutException, InterruptedException {
        final ServiceReference<Gateway> ref = untilNull(context, instanceName, GatewayClient::getGatewayInstance, instanceTimeout);
        try {
            return Optional.of(new GatewayClient(context, ref));
        } catch (final InstanceNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Optional<GatewayClient> tryCreate(@Nonnull final BundleContext context, final String instanceName) {
        return Optional.ofNullable(getGatewayInstance(context, instanceName))
                .map(ref -> {
                    try {
                        return new GatewayClient(context, ref);
                    } catch (final InstanceNotFoundException e) {
                        return null;
                    }
                });
    }

    public static GatewayFilterBuilder filterBuilder() {
        final GatewayFilterBuilder builder = new GatewayFilterBuilder();
        builder.setServiceType(Gateway.class);
        return builder;
    }
    
    private static ServiceReference<Gateway> getGatewayInstance(final BundleContext context,
                                                                final String instanceName) {
        return filterBuilder().setInstanceName(instanceName).getServiceReference(context, Gateway.class).orElse(null);
    }

    /**
     * Adds a new listener for events related to gateway lifecycle.
     * <p>
     *     Event listeners are stored as a weak references therefore
     *     you should hold the strong reference to the listener in the calling code.
     * </p>
     * @param gatewayType Type of gateway.
     * @param listener The listener for events related to gateway with the specified gateway type.
     * @return {@literal true}, if listener is added successfully; {@literal false}, if the specified listener
     * was added previously.
     */
    public static boolean addEventListener(final String gatewayType,
                                        final GatewayEventListener listener){
        return GatewayEventBus.addEventListener(gatewayType, listener);
    }

    /**
     * Removes the listener for events related to gateway lifecycle.
     * @param gatewayType Type of gateway
     * @param listener The listener to remove.
     * @return {@literal true}, if the specified listener is removed successfully; {@literal false},
     * if the specified listener was not added previously using {@link #addEventListener(String, GatewayEventListener)} method.
     */
    public static boolean removeEventListener(final String gatewayType,
                                           final GatewayEventListener listener){
        return GatewayEventBus.removeEventListener(gatewayType, listener);
    }

    private static UnsupportedOperationException unsupportedServiceRequest(final String connectorType,
                                                                           final Class<? extends SupportService> serviceType){
        return new UnsupportedOperationException(String.format("Gateway %s doesn't expose %s service", connectorType, serviceType));
    }

    private static String getGatewayBundleHeader(final BundleContext context,
                                                 final String gatewayType,
                                                 final String header,
                                                 final Locale loc){
        final List<Bundle> candidates = GatewayActivator.getGatewayBundles(context, gatewayType);
        return candidates.isEmpty() ? null : candidates.get(0).getHeaders(loc != null ? loc.toString() : null).get(header);
    }

    /**
     * Gets configuration descriptor for the gateway.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param gatewayType Type of gateway.
     * @param configurationEntity Type of the configuration entity.
     * @param <T> Type of the configuration entity.
     * @return Configuration entity descriptor; or {@literal null}, if configuration description is not supported.
     */
    public static <T extends EntityConfiguration> ConfigurationEntityDescription<T> getConfigurationEntityDescriptor(final BundleContext context,
                                                                                                                     final String gatewayType,
                                                                                                                     final Class<T> configurationEntity) throws UnsupportedOperationException {
        if (context == null || configurationEntity == null) return null;
        ServiceReference<ConfigurationEntityDescriptionProvider> ref = null;
        try {
            ref = filterBuilder()
                    .setGatewayType(gatewayType)
                    .setServiceType(ConfigurationEntityDescriptionProvider.class)
                    .getServiceReference(context, ConfigurationEntityDescriptionProvider.class)
                    .orElseThrow(() -> unsupportedServiceRequest(gatewayType, ConfigurationEntityDescriptionProvider.class));
            final ConfigurationEntityDescriptionProvider provider = context.getService(ref);
            return provider.getDescription(configurationEntity);
        } finally {
            if (ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets bundle state of the specified gateway.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param gatewayType Type of gateway.
     * @return The state of the bundle.
     */
    public static int getState(final BundleContext context, final String gatewayType){
        final List<Bundle> bnds = GatewayActivator.getGatewayBundles(context, gatewayType);
        return bnds.isEmpty() ? Bundle.UNINSTALLED : bnds.get(0).getState();
    }

    /**
     * Gets version of the specified gateway.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param gatewayType Type of gateway.
     * @return The version of the gateway.
     */
    public static Version getVersion(final BundleContext context, final String gatewayType){
        return new Version(getGatewayBundleHeader(context, gatewayType, Constants.BUNDLE_VERSION, null));
    }

    /**
     * Gets localized description of the gateway.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param gatewayType Type of gateway.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the gateway.
     */
    public static String getDescription(final BundleContext context, final String gatewayType, final Locale loc) {
        return getGatewayBundleHeader(context, gatewayType, Constants.BUNDLE_DESCRIPTION, loc);
    }

        /**
         * Gets localized display name of the gateway.
         * @param context The context of the caller bundle. Cannot be {@literal null}.
         * @param gatewayType Type of gateway.
         * @param loc The locale of the display name. May be {@literal null}.
         * @return The display name of the gateway.
         */
    public static String getDisplayName(final BundleContext context, final String gatewayType, final Locale loc) {
        return getGatewayBundleHeader(context, gatewayType, Constants.BUNDLE_NAME, loc);
    }

    /**
     * Gets name of the gateway instance.
     * @return The name of the gateway instance.
     */
    public String getInstanceName() {
        return GatewayFilterBuilder.getGatewayInstance(this);
    }

    public <M extends MBeanFeatureInfo, E extends Exception> boolean forEachFeature(final Class<M> featureType,
                                                            final EntryReader<String, ? super FeatureBindingInfo<M>, E> reader) throws E {
        final Multimap<String, ? extends FeatureBindingInfo<M>> features =
                get().getBindings(featureType);
        for (final String resourceName : features.keySet())
            for (final FeatureBindingInfo<M> bindingInfo : features.get(resourceName))
                if (!reader.accept(resourceName, bindingInfo)) return false;
        return !features.isEmpty();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object.
     */
    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return getService().flatMap(gateway -> gateway.queryObject(objectType));
    }

    /**
     * Gets configuration of this gateway.
     *
     * @return Configuration of this gateway.
     */
    @Nonnull
    @Override
    public Map<String, String> getConfiguration() {
        return getService().map(Gateway::getConfiguration).orElseGet(ImmutableMap::of);
    }

    /**
     * Updates configuration of this gateway.
     *
     * @param configuration A new configuration to update
     * @throws Exception Unable to update this gateway.
     */
    @Override
    public void update(@Nonnull final Map<String, String> configuration) throws Exception {
        get().update(configuration);
    }

    /**
     * Gets state of this instance.
     *
     * @return The state of this instance.
     */
    @Override
    public FrameworkServiceState getState() {
        return getService().map(Gateway::getState).orElse(FrameworkServiceState.CLOSED);
    }

    /**
     * Gets a collection of features contained in this instance of the gateway.
     *
     * @param featureType Type of the feature.
     * @return A collection of features associated with resource name.
     * @see FeatureBindingInfo
     */
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        return getService()
                .<Multimap<String, ? extends FeatureBindingInfo<M>>>map(gateway -> gateway.getBindings(featureType))
                .orElseGet(() -> Multimaps.forMap(ImmutableMap.of()));
    }

    /**
     * Returns system name of this gateway.
     * @return The system name of this gateway.
     */
    @Override
    public String toString() {
        return getInstanceName();
    }

    /**
     * Releases all resources associated with this object.
     */
    @Override
    public void close() {
        release(context);
    }
}
