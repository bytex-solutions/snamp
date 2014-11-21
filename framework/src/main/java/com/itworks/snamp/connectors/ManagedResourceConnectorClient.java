package com.itworks.snamp.connectors;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import com.itworks.snamp.core.SupportService;
import org.osgi.framework.*;

import java.util.*;
import java.util.concurrent.Future;

import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

/**
 * Represents a set of static method that can be used by
 * resource connector client.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagedResourceConnectorClient {
    private ManagedResourceConnectorClient(){

    }

    private static UnsupportedOperationException unsupportedServiceRequest(final String connectorType,
                                                                           final Class<? extends SupportService> serviceType){
        return new UnsupportedOperationException(String.format("Resource connector %s doesn't expose %s service", connectorType, serviceType));
    }

    private static String getConnectorBundleHeader(final BundleContext context,
                                                   final String connectorType,
                                                   final String header,
                                                   final Locale loc){
        final List<Bundle> candidates = AbstractManagedResourceActivator.getResourceConnectorBundles(context, connectorType);
        return candidates.isEmpty() ? null : candidates.get(0).getHeaders(loc != null ? loc.toString() : null).get(header);
    }

    /**
     * Gets a reference to the service exposed by managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param filter Additional service selector. May be {@literal null}.
     * @param serviceType Requested service contract.
     * @param <S> Type of the requested service.
     * @return A reference to the service; or {@literal null}, if service is not available.
     * @throws org.osgi.framework.InvalidSyntaxException Invalid filter.
     */
    public static <S extends FrameworkService> ServiceReference<S> getServiceReference(final BundleContext context,
                                                                                final String connectorType,
                                                                                String filter,
                                                                                final Class<S> serviceType) throws InvalidSyntaxException {
        filter = AbstractManagedResourceActivator.createFilter(connectorType, filter);
        final Collection<ServiceReference<S>> refs = context.getServiceReferences(serviceType, filter);
        return refs.isEmpty() ? null : refs.iterator().next();
    }

    /**
     * Gets bundle state of the specified connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @return The state of the bundle.
     */
    public static int getState(final BundleContext context, final String connectorType){
        final List<Bundle> bnds = AbstractManagedResourceActivator.getResourceConnectorBundles(context, connectorType);
        return bnds.isEmpty() ? Bundle.UNINSTALLED : bnds.get(0).getState();
    }

    /**
     * Gets version of the specified resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @return The version of the connector.
     */
    public static Version getVersion(final BundleContext context, final String connectorType){
        return new Version(getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_VERSION, null));
    }

    /**
     * Gets localized description of the managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the description. May be {@literal null}.
     * @return The description of the connector.
     */
    public static String getDescription(final BundleContext context, final String connectorType, final Locale loc) {
        return getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_DESCRIPTION, loc);
    }

    /**
     * Gets localized display name of the managed resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the display name. May be {@literal null}.
     * @return The display name of the connector.
     */
    public static String getDisplayName(final BundleContext context, final String connectorType, final Locale loc) {
        return getConnectorBundleHeader(context, connectorType, Constants.BUNDLE_NAME, loc);
    }

    /**
     * Gets collection of license limitations associated with the specified connector.
     * <p>
     *     The connector bundle should expose {@link com.itworks.snamp.licensing.LicensingDescriptionService} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the description. May be {@literal null}.
     * @return A map of license limitations with its human-readable description.
     * @throws java.lang.UnsupportedOperationException The specified connector doesn't provide
     *          information about licensing limitations.
     */
    public static Map<String, String> getLicenseLimitations(final BundleContext context,
                                                                                      final String connectorType,
                                                                                      final Locale loc) throws UnsupportedOperationException{
        if(context == null) return null;
        ServiceReference<LicensingDescriptionService> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, LicensingDescriptionService.class);
            if(ref == null)
                throw unsupportedServiceRequest(connectorType, LicensingDescriptionService.class);
            final Map<String, String> result = new HashMap<>(5);
            final LicensingDescriptionService lims = context.getService(ref);
            for(final String limName: lims.getLimitations())
                result.put(limName, lims.getDescription(limName, loc));
            return result;
        }
        catch (final InvalidSyntaxException ignored) {
            return Collections.emptyMap();
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets configuration descriptor for the specified connector.
     * <p>
     *     The connector bundle should expose {@link com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param configurationEntity Type of the configuration entity.
     * @param <T> Type of the configuration entity.
     * @return Configuration entity descriptor.
     * @throws java.lang.UnsupportedOperationException The specified connector doesn't provide
     *          configuration descriptor.
     */
    public static <T extends ConfigurationEntity> ConfigurationEntityDescription<T> getConfigurationEntityDescriptor(final BundleContext context,
                                                          final String connectorType,
                                                          final Class<T> configurationEntity) throws UnsupportedOperationException{
        if(context == null || configurationEntity == null) return null;
        ServiceReference<ConfigurationEntityDescriptionProvider> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, ConfigurationEntityDescriptionProvider.class);
            if(ref == null)
                throw unsupportedServiceRequest(connectorType, ConfigurationEntityDescriptionProvider.class);
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
     * Discovers elements for the managed resource.
     * <p>
     *     The connector bundle should expose {@link com.itworks.snamp.connectors.discovery.DiscoveryService} service.
     * </p>
     * @param context The context of the caller bundle.
     * @param connectorType The system name of the connector.
     * @param connectionString Managed resource connection string.
     * @param connectionOptions Managed resource connection options.
     * @param entityType Type of the managed resource element.
     * @param <T> Type of the managed resource element.
     * @return A collection of discovered managed resource elements
     * @throws java.lang.UnsupportedOperationException Managed resource doesn't support metadata discovery.
     */
    public static <T extends ManagedEntity> Collection<T> discoverEntities(final BundleContext context,
                                                                                          final String connectorType,
                                                                                          final String connectionString,
                                                                                          final Map<String, String> connectionOptions,
                                                                                          final Class<T> entityType) throws UnsupportedOperationException{
        if(context == null || entityType == null) return Collections.emptyList();
        ServiceReference<DiscoveryService> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, DiscoveryService.class);
            if(ref == null) throw unsupportedServiceRequest(connectorType, DiscoveryService.class);
            final DiscoveryService service = context.getService(ref);
            return service.discover(connectionString, connectionOptions, entityType);
        }
        catch (final InvalidSyntaxException ignored) {
            return Collections.emptyList();
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets a collection of connector maintenance actions.
     * <p>
     *     The connector bundle should expose {@link com.itworks.snamp.management.Maintainable} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param loc The locale of the description. May be {@literal null}.
     * @return A map of supported actions and its description.
     * @throws UnsupportedOperationException Resource connector doesn't support maintenance.
     */
    public static Map<String, String> getMaintenanceActions(final BundleContext context,
                                                            final String connectorType,
                                                            final Locale loc) throws UnsupportedOperationException{
        if(context == null) return Collections.emptyMap();
        ServiceReference<Maintainable> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, Maintainable.class);
            if(ref == null) throw unsupportedServiceRequest(connectorType, Maintainable.class);
            final Maintainable service = context.getService(ref);
            final Map<String, String> result = new HashMap<>(service.getActions().size());
            for(final String actionName: service.getActions())
                result.put(actionName, service.getActionDescription(actionName, loc));
            return result;
        }
        catch (final InvalidSyntaxException e) {
            ref = null;
            return Collections.emptyMap();
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Invokes maintenance action.
     * <p>
     *     The connector bundle should expose {@link com.itworks.snamp.management.Maintainable} service.
     * </p>
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorType The system name of the connector.
     * @param actionName The name of the maintenance action to invoke.
     * @param arguments Invocation arguments.
     * @param resultLocale The locale of the input arguments and result.
     * @return An object that represents asynchronous state of the action invocation.
     * @throws UnsupportedOperationException
     */
    public static Future<String> invokeMaintenanceAction(final BundleContext context,
                                                         final String connectorType,
                                                         final String actionName,
                                                         final String arguments,
                                                         final Locale resultLocale) throws UnsupportedOperationException{
        if(context == null) return null;
        ServiceReference<Maintainable> ref = null;
        try {
            ref = getServiceReference(context, connectorType, null, Maintainable.class);
            if(ref == null) throw unsupportedServiceRequest(connectorType, Maintainable.class);
            return context.getService(ref).doAction(actionName, arguments, resultLocale);
        }
        catch (final InvalidSyntaxException e) {
            ref = null;
            return null;
        }
        finally {
            if(ref != null) context.ungetService(ref);
        }
    }

    /**
     * Gets a map of available management connectors in the current OSGi environment.
     * @param context The context of the caller bundle.
     * @return A map of management connector references where the key of the map represents
     *          a name of the management target.
     */
    public static Map<String, ServiceReference<ManagedResourceConnector<?>>> getConnectors(final BundleContext context){
        if(context == null) return Collections.emptyMap();
        else try {
            ServiceReference<?>[] connectors = context.getAllServiceReferences(ManagedResourceConnector.class.getName(), null);
            if(connectors == null) connectors = new ServiceReference<?>[0];
            final Map<String, ServiceReference<ManagedResourceConnector<?>>> result = new HashMap<>(connectors.length);
            for(final ServiceReference<?> serviceRef: connectors) {
                @SuppressWarnings("unchecked")
                final ServiceReference<ManagedResourceConnector<?>> connectorRef = (ServiceReference<ManagedResourceConnector<?>>)serviceRef;
                result.put(AbstractManagedResourceActivator.getManagedResourceName(connectorRef), connectorRef);
            }
            return result;
        }
        catch (final InvalidSyntaxException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Gets type of the management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The type of the management connector.
     */
    public static String getConnectorType(final ServiceReference<ManagedResourceConnector<?>> connectorRef){
        return AbstractManagedResourceActivator.getConnectorType(connectorRef);
    }

    /**
     * Gets connection string used by management connector by its reference.
     * @param connectorRef The reference to the management connector.
     * @return The connection string used by management connector.
     */
    public static String getConnectionString(final ServiceReference<ManagedResourceConnector<?>> connectorRef){
        return AbstractManagedResourceActivator.getConnectionString(connectorRef);
    }

    /**
     * Gets name of the management target that is represented by the specified management
     * connector reference.
     * @param connectorRef The reference to the management connector.
     * @return The name of the management target.
     */
    public static String getManagedResourceName(final ServiceReference<ManagedResourceConnector<?>> connectorRef){
        return AbstractManagedResourceActivator.getManagedResourceName(connectorRef);
    }
}
