package com.itworks.snamp.adapters;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.core.SupportService;
import com.itworks.snamp.management.Maintainable;
import org.osgi.framework.*;

import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static com.itworks.snamp.configuration.AgentConfiguration.EntityConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResourceAdapterClient {
    private static final String LOGGER_NAME = "com.itworks.snamp.adapters.ResourceAdapterClient";

    private final String adapterName;

    /**
     * Initializes a new resource adapter client.
     * @param adapterName The system name of the resource adapter. Cannot be {@literal null} or empty.
     */
    public ResourceAdapterClient(final String adapterName){
        if(Strings.isNullOrEmpty(adapterName))
            throw new IllegalArgumentException("Adapter system name is not defined");
        else this.adapterName = adapterName;
    }

    /**
     * Adds a new listener for events related to resource adapter lifecycle.
     * <p>
     *     Event listeners are stored as a weak references therefore
     *     you should hold the strong reference to the listener in the calling code.
     * </p>
     * @param adapterName The system name of the adapter.
     * @param listener The listener for events related to resource adapter with the specified name.
     * @return {@literal true}, if listener is added successfully; {@literal false}, if the specified listener
     * was added previously.
     */
    public static boolean addEventListener(final String adapterName,
                                        final ResourceAdapterEventListener listener){
        return AbstractResourceAdapter.addEventListener(adapterName, listener);
    }

    public boolean addEventListener(final ResourceAdapterEventListener listener){
        return addEventListener(adapterName, listener);
    }

    /**
     * Removes the listener for events related to resource adapter lifecycle.
     * @param adapterName The system name of the adapter.
     * @param listener The listener to remove.
     * @return {@literal true}, if the specified listener is removed successfully; {@literal false},
     * if the specified listener was not added previously using {@link #addEventListener(String, ResourceAdapterEventListener)} method.
     */
    public static boolean removeEventListener(final String adapterName,
                                           final ResourceAdapterEventListener listener){
        return AbstractResourceAdapter.removeEventListener(adapterName, listener);
    }

    public boolean removeEventListener(final ResourceAdapterEventListener listener){
        return removeEventListener(adapterName, listener);
    }

    private static UnsupportedOperationException unsupportedServiceRequest(final String connectorType,
                                                                           final Class<? extends SupportService> serviceType){
        return new UnsupportedOperationException(String.format("Resource adapter %s doesn't expose %s service", connectorType, serviceType));
    }

    private static String getAdapterBundleHeader(final BundleContext context,
                                                   final String adapterName,
                                                   final String header,
                                                   final Locale loc){
        final List<Bundle> candidates = ResourceAdapterActivator.getResourceAdapterBundles(context, adapterName);
        return candidates.isEmpty() ? null : candidates.get(0).getHeaders(loc != null ? loc.toString() : null).get(header);
    }

    /**
     * Gets a reference to the service exposed by resource adapter.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The system name of the adapter.
     * @param filter Additional service selector. May be {@literal null}.
     * @param serviceType Requested service contract.
     * @param <S> Type of the requested service.
     * @return A reference to the service; or {@literal null}, if service is not available.
     * @throws org.osgi.framework.InvalidSyntaxException Invalid filter.
     */
    public static <S extends FrameworkService> ServiceReference<S> getServiceReference(final BundleContext context,
                                                                                       final String adapterName,
                                                                                       String filter,
                                                                                       final Class<S> serviceType) throws InvalidSyntaxException {
        filter = ResourceAdapterActivator.createFilter(adapterName, filter);
        final Collection<ServiceReference<S>> refs = context.getServiceReferences(serviceType, filter);
        return refs.isEmpty() ? null : refs.iterator().next();
    }

    public <S extends FrameworkService> ServiceReference<S> getServiceReference(final BundleContext context,
                                                                                       final String filter,
                                                                                       final Class<S> serviceType) throws InvalidSyntaxException{
        return getServiceReference(context, adapterName, filter, serviceType);
    }

    /**
     * Gets configuration descriptor for the specified adapter.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The name of the adapter.
     * @param configurationEntity Type of the configuration entity.
     * @param <T> Type of the configuration entity.
     * @return Configuration entity descriptor; or {@literal null}, if configuration description is not supported.
     */
    public static <T extends EntityConfiguration> ConfigurationEntityDescription<T> getConfigurationEntityDescriptor(final BundleContext context,
                                                                                                                     final String adapterName,
                                                                                                                     final Class<T> configurationEntity) throws UnsupportedOperationException{
        if(context == null || configurationEntity == null) return null;
        ServiceReference<ConfigurationEntityDescriptionProvider> ref = null;
        try {
            ref = getServiceReference(context, adapterName, null, ConfigurationEntityDescriptionProvider.class);
            if(ref == null)
                throw unsupportedServiceRequest(adapterName, ConfigurationEntityDescriptionProvider.class);
            final ConfigurationEntityDescriptionProvider provider = context.getService(ref);
            return provider.getDescription(configurationEntity);
        }
        catch (final InvalidSyntaxException e) {
            ref = null;
            try(final OSGiLoggingContext logger = OSGiLoggingContext.getLogger(LOGGER_NAME, context)){
                logger.log(Level.SEVERE, String.format("Unable to discover configuration schema of %s adapter", adapterName), e);
            }
            return null;
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    public <T extends EntityConfiguration> ConfigurationEntityDescription<T> getConfigurationEntityDescriptor(final BundleContext context,
                                                                                                              final Class<T> configurationEntity) throws UnsupportedOperationException{
        return getConfigurationEntityDescriptor(context, adapterName, configurationEntity);
    }

    /**
     * Gets a collection of adapter maintenance actions.
     * <p>
     *     The adapter bundle should expose {@link com.itworks.snamp.management.Maintainable} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The system name of the adapter.
     * @param loc The locale of the description. May be {@literal null}.
     * @return A map of supported actions and its description.
     */
    public static Map<String, String> getMaintenanceActions(final BundleContext context,
                                                            final String adapterName,
                                                            final Locale loc) throws UnsupportedOperationException{
        if(context == null) return Collections.emptyMap();
        ServiceReference<Maintainable> ref = null;
        try {
            ref = getServiceReference(context, adapterName, null, Maintainable.class);
            if(ref == null) throw unsupportedServiceRequest(adapterName, Maintainable.class);
            final Maintainable service = context.getService(ref);
            final Map<String, String> result = Maps.newHashMapWithExpectedSize(service.getActions().size());
            for(final String actionName: service.getActions())
                result.put(actionName, service.getActionDescription(actionName, loc));
            return result;
        }
        catch (final InvalidSyntaxException e) {
            ref = null;
            try(final OSGiLoggingContext logger = OSGiLoggingContext.getLogger(LOGGER_NAME, context)){
                logger.log(Level.SEVERE, String.format("Unable to enumerate maintenance actions of %s adapter", adapterName), e);
            }
            return Collections.emptyMap();
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    public Map<String, String> getMaintenanceActions(final BundleContext context,
                                                            final Locale loc) throws UnsupportedOperationException{
        return getMaintenanceActions(context, adapterName, loc);
    }

    /**
     * Invokes maintenance action.
     * <p>
     *     The adapter bundle should expose {@link com.itworks.snamp.management.Maintainable} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The system name of the adapter.
     * @param actionName The name of the maintenance action to invoke.
     * @param arguments Invocation arguments.
     * @param resultLocale The locale of the input arguments and result.
     * @return An object that represents asynchronous state of the action invocation.
     */
    public static Future<String> invokeMaintenanceAction(final BundleContext context,
                                                         final String adapterName,
                                                         final String actionName,
                                                         final String arguments,
                                                         final Locale resultLocale) throws UnsupportedOperationException {
        if (context == null) return null;
        ServiceReference<Maintainable> ref = null;
        try {
            ref = getServiceReference(context, adapterName, null, Maintainable.class);
            if (ref == null) throw unsupportedServiceRequest(adapterName, Maintainable.class);
            return context.getService(ref).doAction(actionName, arguments, resultLocale);
        } catch (final InvalidSyntaxException e) {
            return Futures.immediateFailedCheckedFuture(e);
        } finally {
            if (ref != null) context.ungetService(ref);
        }
    }

    public Future<String> invokeMaintenanceAction(final BundleContext context,
                                                         final String actionName,
                                                         final String arguments,
                                                         final Locale resultLocale) throws UnsupportedOperationException{
        return invokeMaintenanceAction(context, adapterName, actionName, arguments, resultLocale);
    }

    /**
     * Gets bundle state of the specified adapter.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The system name of the adapter.
     * @return The state of the bundle.
     */
    public static int getState(final BundleContext context, final String adapterName){
        final List<Bundle> bnds = ResourceAdapterActivator.getResourceAdapterBundles(context, adapterName);
        return bnds.isEmpty() ? Bundle.UNINSTALLED : bnds.get(0).getState();
    }

    public int getState(final BundleContext context){
        return getState(context, adapterName);
    }

    /**
     * Gets version of the specified resource adapter.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The system name of the adapter.
     * @return The version of the adapter.
     */
    public static Version getVersion(final BundleContext context, final String adapterName){
        return new Version(getAdapterBundleHeader(context, adapterName, Constants.BUNDLE_VERSION, null));
    }

    public Version getVersion(final BundleContext context){
        return getVersion(context, adapterName);
    }

    /**
     * Gets localized description of the resource adapter.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The system name of the adapter.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the adapter.
     */
    public static String getDescription(final BundleContext context, final String adapterName, final Locale loc) {
        return getAdapterBundleHeader(context, adapterName, Constants.BUNDLE_DESCRIPTION, loc);
    }

    public String getDescription(final BundleContext context, final Locale loc) {
        return getDescription(context, adapterName, loc);
    }

        /**
         * Gets localized display name of the resource adapter.
         * @param context The context of the caller bundle. Cannot be {@literal null}.
         * @param adapterName The system name of the adapter.
         * @param loc The locale of the display name. May be {@literal null}.
         * @return The display name of the adapter.
         */
    public static String getDisplayName(final BundleContext context, final String adapterName, final Locale loc) {
        return getAdapterBundleHeader(context, adapterName, Constants.BUNDLE_NAME, loc);
    }

    public String getDisplayName(final BundleContext context, final Locale loc){
        return getDisplayName(context, adapterName, loc);
    }

    /**
     * Returns system name of this adapter.
     * @return The system name of this adapter.
     */
    @Override
    public String toString() {
        return adapterName;
    }
}
