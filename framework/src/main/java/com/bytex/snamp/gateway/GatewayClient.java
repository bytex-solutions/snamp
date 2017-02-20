package com.bytex.snamp.gateway;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.osgi.framework.*;

import javax.management.MBeanFeatureInfo;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.concurrent.SpinWait.spinUntilNull;
import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents a client of resource connector that can be used by gateway consumers.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GatewayClient extends ServiceHolder<Gateway> {
    /**
     * Initializes a new gateway client.
     *
     * @param context    The context of the bundle which holds this reference. Cannot be {@literal null}.
     * @param serviceRef The service reference to wrap. Cannot be {@literal null}.
     */
    public GatewayClient(final BundleContext context, final ServiceReference<Gateway> serviceRef) {
        super(context, serviceRef);
    }

    public static GatewayClient tryCreate(final BundleContext context,
                                          final String instanceName,
                                          final Duration instanceTimeout) throws TimeoutException, InterruptedException{
        final ServiceReference<Gateway> ref = spinUntilNull(context, instanceName, GatewayClient::getGatewayInstance, instanceTimeout);
        return new GatewayClient(context, ref);
    }

    public static GatewayClient tryCreate(final BundleContext context, final String instanceName) {
        final ServiceReference<Gateway> ref = getGatewayInstance(context, instanceName);
        return ref == null ? null : new GatewayClient(context, ref);
    }

    /**
     * Obtains a reference to the instance of gateway.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param instanceName The name of the instance.
     * @return A reference to the instance of gateway; or {@literal null} if instance doesn't exist.
     */
    @SuppressWarnings("unchecked")
    private static ServiceReference<Gateway> getGatewayInstance(final BundleContext context,
                                                                final String instanceName) {
        return callUnchecked(() -> Iterables.<ServiceReference>getFirst(context.getServiceReferences(Gateway.class, GatewayActivator.createFilter(instanceName)), null));
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
     * Gets a reference to the service exposed by gateway.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param gatewayType Type of gateway.
     * @param filter Additional service selector. May be {@literal null}.
     * @param serviceType Requested service contract.
     * @param <S> Type of the requested service.
     * @return A reference to the service; or {@literal null}, if service is not available.
     * @throws org.osgi.framework.InvalidSyntaxException Invalid filter.
     */
    public static <S extends FrameworkService> ServiceReference<S> getServiceReference(final BundleContext context,
                                                                                       final String gatewayType,
                                                                                       String filter,
                                                                                       final Class<S> serviceType) throws InvalidSyntaxException {
        filter = GatewayActivator.createFilter(gatewayType, filter);
        final Collection<ServiceReference<S>> refs = context.getServiceReferences(serviceType, filter);
        return refs.isEmpty() ? null : refs.iterator().next();
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
                                                                                                                     final Class<T> configurationEntity) throws UnsupportedOperationException{
        if(context == null || configurationEntity == null) return null;
        ServiceReference<ConfigurationEntityDescriptionProvider> ref = null;
        try {
            ref = getServiceReference(context, gatewayType, null, ConfigurationEntityDescriptionProvider.class);
            if(ref == null)
                throw unsupportedServiceRequest(gatewayType, ConfigurationEntityDescriptionProvider.class);
            final ConfigurationEntityDescriptionProvider provider = context.getService(ref);
            return provider.getDescription(configurationEntity);
        }
        catch (final InvalidSyntaxException ignored) {
            ref = null;
            return null;
        }
        finally {
            if(ref != null) context.ungetService(ref);
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
        return GatewayActivator.getGatewayInstance(this);
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
     * Returns system name of this gateway.
     * @return The system name of this gateway.
     */
    @Override
    public String toString() {
        return getInstanceName();
    }
}
