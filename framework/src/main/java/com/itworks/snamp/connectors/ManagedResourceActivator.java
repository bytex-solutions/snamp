package com.itworks.snamp.connectors;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.connectors.discovery.AbstractDiscoveryService;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.core.OsgiLoggingContext;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.LicenseLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Represents a base class for management connector bundle.
 * <p>
 *     This bundle activator represents a factory of {@link ManagedResourceConnector} implementations.
 *     Each connector should be registered as separated service in OSGi environment.
 * </p>
 * @param <TConnector> Type of the management connector.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class ManagedResourceActivator<TConnector extends ManagedResourceConnector<?>> extends AbstractServiceLibrary {
    /**
     * Represents name of the manifest header which contains the name of the management connector.
     * <p>
     *     The following example demonstrates how to set the name of the management connector
     *     in the connector's bundle manifest:
     *     <pre><tt>
     *          SNAMP-Resource-Connector: impl
     *     </tt></pre>
     * </p>
     */
    public static final String CONNECTOR_NAME_MANIFEST_HEADER = "SNAMP-Resource-Connector";
    private static final String MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY = "managedResource";
    private static final String CONNECTOR_STRING_IDENTITY_PROPERTY = "connectionString";
    private static final String CONNECTOR_TYPE_IDENTITY_PROPERTY = CONNECTOR_NAME_MANIFEST_HEADER;

    private static final ActivationProperty<String> CONNECTOR_TYPE_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents an interface responsible for lifecycle control over resource connector instances.
     * @param <TConnector> Type of the managed resource connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ManagedResourceConnectorLifecycleController<TConnector extends ManagedResourceConnector<?>>{

        /**
         * Creates a new instance of the managed resource connector.
         * @param resourceName The name of the managed resource.
         * @param connectionString Managed resource connection string.
         * @param connectionParameters Connection parameters.
         * @param dependencies A collection of connector dependencies.
         * @return A new instance of the resource connector.
         * @throws Exception Unable to instantiate managed resource connector.
         */
        TConnector createConnector(final String resourceName, final String connectionString,
                                   final Map<String, String> connectionParameters,
                                   final RequiredService<?>... dependencies) throws Exception;

        /**
         * Updates the resource connector with a new configuration.
         * @param connector The instance of the connector to update.
         * @param resourceName The name of the managed resource.
         * @param connectionString A new managed resource connection string.
         * @param connectionParameters A new connection parameters.
         * @param dependencies A collection of connector dependencies.
         * @return An updated resource connector.
         * @throws Exception Unable to update managed resource connector.
         */
        TConnector updateConnector(final TConnector connector,
                                   final String resourceName,
                                   final String connectionString,
                                   final Map<String, String> connectionParameters,
                                   final RequiredService<?>... dependencies) throws Exception;

        /**
         * Releases all resources associated with the resource connector.
         * @param connector The instance of the connector to dispose.
         * @throws Exception Unable to dispose resource connector instance.
         */
        void releaseConnector(final TConnector connector) throws Exception;
    }

    /**
     * Represents managed resource connector factory.
     * <p>
     *     This class provides the default implementation of {@link ManagedResourceActivator.ManagedResourceConnectorLifecycleController#releaseConnector(ManagedResourceConnector)}
     *     and {@link ManagedResourceActivator.ManagedResourceConnectorLifecycleController#updateConnector(ManagedResourceConnector, String, String, java.util.Map, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}
     *     in the following manner:
     *     <ul>
     *         <li>{@code releaseConnector} - just calls {@link AutoCloseable#close()} method of the connector.</li>
     *         <li>{@code updateConnector} - sequentially calls {@code releaseConnector} on the existing connector
     *         and creates a new instance of the connector using {@code createConnector}.</li>
     *     </ul>
     *     In the derived factory you may implements just {@link ManagedResourceActivator.ManagedResourceConnectorLifecycleController#createConnector(String, String, java.util.Map, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}
     *     method, but other methods are available for overriding.
     * </p>
     * @param <TConnector> Type of the connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ManagedResourceConnectorFactory<TConnector extends ManagedResourceConnector<?> & AutoCloseable> implements ManagedResourceConnectorLifecycleController<TConnector>{

        /**
         * Updates the resource connector with a new configuration.
         *
         * @param connector            The instance of the connector to update.
         * @param resourceName         The name of the managed resource.
         * @param connectionString     A new managed resource connection string.
         * @param connectionParameters A new connection parameters.
         * @param dependencies A collection of connector dependencies.
         * @return An updated resource connector.
         * @throws Exception Unable to update managed resource connector.
         */
        @Override
        public TConnector updateConnector(TConnector connector,
                                          final String resourceName,
                                          final String connectionString,
                                          final Map<String, String> connectionParameters,
                                          final RequiredService<?>... dependencies) throws Exception {
            releaseConnector(connector);
            return createConnector(resourceName, connectionString, connectionParameters, dependencies);
        }

        /**
         * Releases all resources associated with the resource connector.
         * <p>
         *     This method just calls {@link AutoCloseable#close()} implemented in the connector.
         * </p>
         * @param connector The instance of the connector to dispose.
         * @throws Exception Unable to dispose resource connector instance.
         */
        @Override
        public void releaseConnector(final TConnector connector) throws Exception {
            connector.close();
        }
    }

    private static final class ManagedResourceConnectorRegistry<TConnector extends ManagedResourceConnector<?>> extends ServiceSubRegistryManager<ManagedResourceConnector, TConnector>{
        private static final String NOTIF_TRANSPORT_LISTENER_ID = "EventAdminTransport";

        private final ManagedResourceConnectorLifecycleController<TConnector> controller;
        /**
         * Represents name of the managed resource connector.
         */
        protected final String connectorType;

        private ManagedResourceConnectorRegistry(final String connectorType,
                                                 final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                                 final RequiredService<?>... dependencies){
            super(ManagedResourceConnector.class,
                    PersistentConfigurationManager.getConnectorFactoryPersistentID(connectorType),
                    ArrayUtils.addToEnd(dependencies, new SimpleDependency<>(EventAdmin.class), RequiredService.class));
            this.controller = Objects.requireNonNull(controller, "controller is null.");
            this.connectorType = connectorType;
        }

        private OsgiLoggingContext getLoggingContext() {
            Logger logger = getActivationPropertyValue(LOGGER_HOLDER);
            if (logger == null)
                logger = AbstractManagedResourceConnector.getLogger(connectorType);
            return OsgiLoggingContext.get(logger,
                    Utils.getBundleContextByObject(controller));
        }

        /**
         * Updates the service with a new configuration.
         *
         * @param oldConnector       The service to update.
         * @param configuration A new configuration of the service.
         * @return The updated service.
         * @throws Exception                                  Unable to update service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected TConnector update(final TConnector oldConnector,
                                        final Dictionary<String, ?> configuration,
                                        final RequiredService<?>... dependencies) throws Exception {
            final String resourceName = PersistentConfigurationManager.getResourceName(configuration);
            final TConnector newConnector = controller.updateConnector(oldConnector,
                    resourceName,
                    PersistentConfigurationManager.getConnectionString(configuration),
                    PersistentConfigurationManager.getResourceConnectorParameters(configuration),
                    dependencies);
            if(newConnector != oldConnector)
                subscribe(newConnector.queryObject(NotificationSupport.class), resourceName, findDependency(RequiredServiceAccessor.class, EventAdmin.class, dependencies));
            return newConnector;
        }

        private void subscribe(final NotificationSupport connector,
                               final String resourceName,
                               final RequiredServiceAccessor<EventAdmin> eventAdmin) throws NotificationSupportException, UnknownSubscriptionException {
            if(connector != null && eventAdmin != null) {
                connector.unsubscribe(NOTIF_TRANSPORT_LISTENER_ID);
                connector.subscribe(NOTIF_TRANSPORT_LISTENER_ID, new EventAdminTransport(connectorType, resourceName, eventAdmin, connector), true);
            }
            else try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.info(String.format("Resource %s doesn't support notifications", resourceName));
            }
        }

        private static void unsubscribe(final NotificationSupport connector){
            if(connector != null)
                connector.unsubscribe(NOTIF_TRANSPORT_LISTENER_ID);
        }

        /**
         * Logs error details when {@link #activateService(String, java.util.Dictionary, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])} failed.
         *
         * @param servicePID    The persistent identifier associated with a newly created service.
         * @param configuration The configuration of the service.
         * @param e             An exception occurred when instantiating service.
         */
        @Override
        protected void failedToActivateService(final String servicePID, final Dictionary<String, ?> configuration, final Exception e) {
            try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to instantiate connector. Connection string: %s, connection parameters: %s",
                        PersistentConfigurationManager.getConnectionString(configuration),
                        PersistentConfigurationManager.getResourceConnectorParameters(configuration)), e);
            }
        }

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])} failed.
         *
         * @param servicePID    The persistent identifier associated with the service.
         * @param configuration The configuration of the service.
         * @param e             An exception occurred when updating service.
         */
        @Override
        protected void failedToUpdateService(final String servicePID, final Dictionary<String, ?> configuration, final Exception e) {
            try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to update connector. Connection string: %s, connection parameters: %s",
                        PersistentConfigurationManager.getConnectionString(configuration),
                        PersistentConfigurationManager.getResourceConnectorParameters(configuration)),
                        e);
            }
        }

        /**
         * Logs error details when {@link #dispose(Object, boolean)} failed.
         *
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e          An exception occurred when disposing service.
         */
        @Override
        protected void failedToCleanupService(final String servicePID, final Exception e) {
            try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, "Unable to dispose connector", e);
            }
        }

        /**
         * Creates a new service.
         *
         * @param identity      The registration properties to fill.
         * @param configuration A new configuration of the service.
         * @param dependencies  The dependencies required for the service.
         * @return A new instance of the service.
         * @throws Exception                                  Unable to instantiate a new service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid configuration exception.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected TConnector createService(final Map<String, Object> identity,
                                               final Dictionary<String, ?> configuration,
                                               final RequiredService<?>... dependencies) throws Exception {
            final String resourceName = PersistentConfigurationManager.getResourceName(configuration);
            final Map<String, String> options = PersistentConfigurationManager.getResourceConnectorParameters(configuration);
            final String connectionString = PersistentConfigurationManager.getConnectionString(configuration);
            createIdentity(resourceName,
                    connectorType,
                    connectionString,
                    options,
                    identity);
            final TConnector result = controller.createConnector(resourceName, connectionString, options, dependencies);
            subscribe(result.queryObject(NotificationSupport.class), resourceName, findDependency(RequiredServiceAccessor.class, EventAdmin.class, dependencies));
            return result;
        }

        /**
         * Releases all resources associated with the service instance.
         *
         * @param service A service to dispose.
         * @throws Exception Unable to dispose service.
         */
        @Override
        protected void cleanupService(final TConnector service) throws Exception {
            unsubscribe(service.queryObject(NotificationSupport.class));
            controller.releaseConnector(service);
        }
    }

    /**
     * Represents superclass for all-optional resource connector service providers.
     * You cannot derive from this class directly.
     * @param <S> Type of the adapter-related service contract.
     * @param <T> Type of the adapter-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     * @see ManagedResourceActivator.ConfigurationEntityDescriptionManager
     * @see ManagedResourceActivator.DiscoveryServiceManager
     * @see ManagedResourceActivator.LicensingDescriptionServiceManager
     */
    protected static abstract class SupportConnectorServiceManager<S extends FrameworkService, T extends S> extends ProvidedService<S, T> {

        private SupportConnectorServiceManager(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        /**
         * Gets name of the underlying resource connector.
         * <p>
         *     This property is available when this manager is in {@link com.itworks.snamp.core.AbstractBundleActivator.ActivationState#ACTIVATED}
         *     state only.
         * </p>
         * @return The name of the underlying resource connector.
         * @see #getState()
         */
        protected final String getConnectorName() {
            return getActivationPropertyValue(CONNECTOR_TYPE_HOLDER);
        }

        /**
         * Gets logger associated with the managed resource connector.
         * <p>
         *     This property is available when this manager is in {@link com.itworks.snamp.core.AbstractBundleActivator.ActivationState#ACTIVATED}
         *     state only.
         * </p>
         * @return A logger associated with the managed resource connector.
         * @see #getState()
         */
        protected final Logger getLogger() {
            return getActivationPropertyValue(LOGGER_HOLDER);
        }
    }

    /**
     * Represents maintenance service provider.
     * @param <T> Type of the maintenance service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class MaintenanceServiceManager<T extends Maintainable> extends SupportConnectorServiceManager<Maintainable,T> {

        protected MaintenanceServiceManager(final RequiredService<?>... dependencies) {
            super(Maintainable.class, dependencies);
        }

        protected abstract T createMaintenanceService(final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected final T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(CONNECTOR_TYPE_IDENTITY_PROPERTY, getConnectorName());
            return createMaintenanceService(dependencies);
        }
    }

    private static final class ConnectorLicensingDescriptorService<L extends LicenseLimitations> extends AbstractAggregator implements LicensingDescriptionService {
        private final LicenseReader licenseReader;
        private final Class<L> descriptor;
        private final Supplier<L> fallbackFactory;
        private final Logger logger;

        private ConnectorLicensingDescriptorService(final LicenseReader reader,
                                                   final Class<L> descriptor,
                                                   final Supplier<L> fallbackFactory,
                                                   final Logger logger){
            this.licenseReader = reader;
            this.descriptor = descriptor;
            this.fallbackFactory = fallbackFactory;
            this.logger = logger;
        }

        /**
         * Gets logger associated with this service.
         *
         * @return The logger associated with this service.
         */
        @Override
        public Logger getLogger() {
            return logger;
        }

        /**
         * Gets a read-only collection of license limitations.
         *
         * @return A read-only collection of license limitations.
         */
        @Override
        public final Collection<String> getLimitations() {
            return Lists.newArrayList((licenseReader.getLimitations(descriptor, fallbackFactory)));
        }

        /**
         * Gets human-readable description of the specified limitation.
         *
         * @param limitationName The system name of the limitation.
         * @param loc            The locale of the description. May be {@literal null}.
         * @return The description of the limitation.
         */
        @Override
        public final String getDescription(final String limitationName, final Locale loc) {
            final LicenseLimitations.Limitation<?> lim =  licenseReader.getLimitations(descriptor, fallbackFactory).getLimitation(limitationName);
            return lim != null ? lim.getDescription(loc) : "";
        }
    }

    protected static class LicensingDescriptionServiceManager<L extends LicenseLimitations> extends SupportConnectorServiceManager<LicensingDescriptionService, ConnectorLicensingDescriptorService> {
        private final Supplier<L> fallbackFactory;
        private final Class<L> descriptor;

        public LicensingDescriptionServiceManager(final Class<L> limitationsDescriptor,
                                                  final Supplier<L> fallbackFactory) {
            super(LicensingDescriptionService.class, new SimpleDependency<>(LicenseReader.class));
            if(fallbackFactory == null) throw new IllegalArgumentException("fallbackFactory is null.");
            else if(limitationsDescriptor == null) throw new IllegalArgumentException("limitationsDescriptor is null.");
            else{
                this.fallbackFactory = fallbackFactory;
                this.descriptor = limitationsDescriptor;
            }
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected final ConnectorLicensingDescriptorService activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            identity.put(CONNECTOR_TYPE_IDENTITY_PROPERTY, getConnectorName());
            return new ConnectorLicensingDescriptorService(getDependency(SimpleDependency.class, LicenseReader.class, dependencies),
                    descriptor,
                    fallbackFactory,
                    getActivationPropertyValue(LOGGER_HOLDER));
        }
    }

    /**
     * Represents simple manager that exposes default implementation of {@link com.itworks.snamp.connectors.discovery.DiscoveryService}.
     * <p>
     *     Discovery algorithm is based on the abstract methods that you should implement in the derived manager.
     *     Note that these methods should be stateless.
     * @param <TProvider> Type of management information provider.
     * @author Roman Sakno
     * @since 1.0
     */
    protected abstract static class SimpleDiscoveryServiceManager<TProvider extends AutoCloseable> extends DiscoveryServiceManager<AbstractDiscoveryService<TProvider>>{
        /**
         * Initializes a new instance of the discovery service manager.
         * @param dependencies A collection of discovery service dependencies.
         */
        protected SimpleDiscoveryServiceManager(final RequiredService<?>... dependencies){
            super(dependencies);
        }

        /**
         * Creates a new instance of the management information provider.
         * <p>
         *     This method should be stateless.
         * @param connectionString Managed resource connection string.
         * @param connectionOptions Managed resource connection options.
         * @param dependencies A collection of discovery service dependencies.
         * @return A new instance of the management information provider.
         * @throws Exception Unable to instantiate provider.
         */
        protected abstract TProvider createManagementInformationProvider(final String connectionString,
                                                                         final Map<String, String> connectionOptions,
                                                                         final RequiredService<?>... dependencies) throws Exception;

        /**
         * Extracts management information from provider.
         * <p>
         *     This method should be stateless.
         * @param entityType Type of the requested management information.
         * @param provider An instance of the management information provider.
         * @param dependencies A collection of discovery service dependencies.
         * @param <T> Type of the requested management information.
         * @return A new instance of the management information provider.
         * @throws Exception Unable to extract management information.
         */
        protected abstract <T extends ManagedResourceConfiguration.ManagedEntity> Collection<T> getManagementInformation(final Class<T> entityType,
                                                                                                                         final TProvider provider,
                                                                                                                         final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the discovery service.
         *
         * @param dependencies A collection of discovery service dependencies.
         * @return A new instance of the discovery service.
         * @throws Exception Unable to instantiate discovery service.
         */
        @Override
        protected final AbstractDiscoveryService<TProvider> createDiscoveryService(final RequiredService<?>... dependencies) throws Exception {
            return new AbstractDiscoveryService<TProvider>() {
                private final Logger logger = getActivationPropertyValue(LOGGER_HOLDER);

                @Override
                protected TProvider createProvider(final String connectionString, final Map<String, String> connectionOptions) throws Exception {
                    return createManagementInformationProvider(connectionString, connectionOptions, dependencies);
                }

                @Override
                protected <T extends ManagedResourceConfiguration.ManagedEntity> Collection<T> getEntities(final Class<T> entityType, final TProvider provider) throws Exception {
                    return getManagementInformation(entityType, provider, dependencies);
                }

                @Override
                public Logger getLogger() {
                    return logger;
                }
            };
        }

        /**
         * Provides service cleanup operations.
         * <p>
         * In the default implementation this method does nothing.
         *
         * @param serviceInstance An instance of the hosted service to cleanup.
         * @param stopBundle      {@literal true}, if this method calls when the owner bundle is stopping;
         *                        {@literal false}, if this method calls when loosing dependency.
         */
        @Override
        @MethodStub
        protected final void cleanupService(final AbstractDiscoveryService<TProvider> serviceInstance, final boolean stopBundle) {
            //do nothing
        }
    }

    /**
     * Represents factory for {@link com.itworks.snamp.connectors.discovery.DiscoveryService} service.
     * @param <T> A class that provides implementation of {@link com.itworks.snamp.connectors.discovery.DiscoveryService}
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected abstract static class DiscoveryServiceManager<T extends DiscoveryService> extends SupportConnectorServiceManager<DiscoveryService, T> {

        /**
         * Initializes a new holder for the provided service.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected DiscoveryServiceManager(final RequiredService<?>... dependencies) {
            super(DiscoveryService.class, dependencies);
        }

        /**
         * Creates a new instance of the discovery service.
         * @param dependencies A collection of discovery service dependencies.
         * @return A new instance of the discovery service.
         * @throws java.lang.Exception Unable to instantiate discovery service.
         */
        protected abstract T createDiscoveryService(final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         * @throws java.lang.Exception Unable to instantiate discovery service.
         */
        @Override
        protected final T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(CONNECTOR_TYPE_IDENTITY_PROPERTY, getConnectorName());
            return createDiscoveryService(dependencies);
        }
    }

    /**
     * Represents a simple implementation of configuration description service manager based
     * on provided array of descriptions for each {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity}.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected final static class SimpleConfigurationEntityDescriptionManager extends ConfigurationEntityDescriptionManager<ConfigurationEntityDescriptionProviderImpl>{
        private final ConfigurationEntityDescription<?>[] descriptions;

        /**
         * Initializes a new configuration service manager.
         * @param descriptions An array of configuration entity descriptors.
         */
        public SimpleConfigurationEntityDescriptionManager(final ConfigurationEntityDescription<?>... descriptions){
            this.descriptions = Arrays.copyOf(descriptions, 0);
        }

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         */
        @Override
        protected ConfigurationEntityDescriptionProviderImpl createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new ConfigurationEntityDescriptionProviderImpl(descriptions);
        }

        /**
         * Provides service cleanup operations.
         * <p>
         * In the default implementation this method does nothing.
         * </p>
         *
         * @param serviceInstance An instance of the hosted service to cleanup.
         * @param stopBundle      {@literal true}, if this method calls when the owner bundle is stopping;
         *                        {@literal false}, if this method calls when loosing dependency.
         */
        @Override
        @MethodStub
        protected void cleanupService(final ConfigurationEntityDescriptionProviderImpl serviceInstance, final boolean stopBundle) throws Exception {
            //nothing to do
        }
    }

    /**
     * Represents a holder for connector configuration descriptor.
     * @param <T> Type of the configuration descriptor implementation.
     * @author Roman Sakno
     * @since 1.0
     */
    protected abstract static class ConfigurationEntityDescriptionManager<T extends ConfigurationEntityDescriptionProvider> extends SupportConnectorServiceManager<ConfigurationEntityDescriptionProvider, T> {

        /**
         * Initializes a new holder for the provided service.
         *
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected ConfigurationEntityDescriptionManager(final RequiredService<?>... dependencies) {
            super(ConfigurationEntityDescriptionProvider.class, dependencies);
        }

        /**
         * Creates a new instance of the configuration description provider.
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         * @throws Exception An exception occurred during provider instantiation.
         */
        protected abstract T createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected final T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(CONNECTOR_TYPE_IDENTITY_PROPERTY, getConnectorName());
            return createConfigurationDescriptionProvider(dependencies);
        }
    }

    /**
     * Represents transport for {@link com.itworks.snamp.connectors.notifications.Notification} object
     * through OSGi environment using {@link org.osgi.service.event.EventAdmin} service.
     * This class cannot be inherited.
     */
    private static final class EventAdminTransport implements NotificationListener{
        private final String connectorName;
        private final RequiredServiceAccessor<EventAdmin> eventAdmin;
        private final Reference<NotificationSupport> notifications;
        private final String resourceName;

        private EventAdminTransport(final String connectorName,
                                   final String resourceName,
                                   final RequiredServiceAccessor<EventAdmin> dependency,
                                   final NotificationSupport notifSupport){
            this.eventAdmin = dependency;
            this.notifications = new WeakReference<>(notifSupport);
            this.connectorName = connectorName;
            this.resourceName = resourceName;
        }

        /**
         * Handles the specified notification.
         *
         * @param listId An identifier of the subscription list.
         * @param n      The notification to handle.
         * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
         */
        @Override
        public final boolean handle(final String listId, final Notification n) {
            final NotificationSupport notifSupport = notifications.get();
            if(eventAdmin.isResolved() && notifSupport != null){
                final NotificationMetadata metadata = notifSupport.getNotificationInfo(listId);
                if(metadata == null) return false;
                final NotificationEvent event = new NotificationEvent(n, resourceName, listId);
                eventAdmin.getService().postEvent(event.toEvent(connectorName, metadata.getCategory()));
                return true;
            }
            else return false;
        }
    }

    /**
     * Represents name of the management connector.
     */
    public final String connectorType;

    /**
     * Initializes a new connector factory.
     * @param connectorType The name of the connector.
     * @param controller Resource connector lifecycle controller. Cannot be {@literal null}.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final String connectorType,
                                       final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                       final SupportConnectorServiceManager<?, ?>... optionalServices){
        this(connectorType,
                controller,
                EMTPY_REQUIRED_SERVICES,
                optionalServices);
    }

    /**
     * Initializes a new connector factory.
     * @param connectorType The name of the connector.
     * @param controller Resource connector lifecycle controller. Cannot be {@literal null}.
     * @param connectorDependencies A collection of connector-level dependencies.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final String connectorType,
                                       final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                       final RequiredService<?>[] connectorDependencies,
                                       final SupportConnectorServiceManager<?, ?>[] optionalServices){
        super(ArrayUtils.addToEnd(optionalServices, new ManagedResourceConnectorRegistry<>(connectorType, controller, connectorDependencies), ProvidedService.class));
        this.connectorType = connectorType;
    }

    private static void createIdentity(final String resourceName,
                                       final String connectorType,
                                       final String connectionString,
                                       final Map<String, String> connectionOptions,
                                       final Map<String, Object> identity){
        identity.put(MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, resourceName);
        identity.put(CONNECTOR_TYPE_IDENTITY_PROPERTY, connectorType);
        identity.put(CONNECTOR_STRING_IDENTITY_PROPERTY, connectionString);
        identity.putAll(connectionOptions);
    }

    /**
     * Adds global dependencies.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param dependencies A collection of connector's global dependencies.
     */
    @SuppressWarnings("UnusedParameters")
    @MethodStub
    protected void addDependencies(final Collection<RequiredService<?>> dependencies){

    }

    /**
     * Initializes the library.
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     */
    @Override
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
        addDependencies(bundleLevelDependencies);
    }

    /**
     * Activates this service library.
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected final void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        activationProperties.publish(LOGGER_HOLDER, getLogger());
        activationProperties.publish(CONNECTOR_TYPE_HOLDER, connectorType);
        try(final OsgiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Activating resource connectors of type %s", connectorType));
        }
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(connectorType);
    }

    private OsgiLoggingContext getLoggingContext(){
        return OsgiLoggingContext.get(getLogger(), Utils.getBundleContextByObject(this));
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.itworks.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        try (final OsgiLoggingContext logger = getLoggingContext()) {
            logger.log(Level.SEVERE, String.format("Unable to instantiate %s connector", connectorType), e);
        }
    }

    /**
     * Handles an exception thrown by {@link } method.
     *
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        try (final OsgiLoggingContext logger = getLoggingContext()) {
            logger.log(Level.SEVERE, String.format("Unable to release %s connector instance", connectorType), e);
        }
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected final void deactivate(final ActivationPropertyReader activationProperties) {
        try(final OsgiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Unloading connectors of type %s", connectorType));
        }
    }

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return connectorType;
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    public final boolean equals(final ManagedResourceActivator<?> factory){
        return factory != null && connectorType.equals(factory.connectorType);
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    @Override
    public final boolean equals(final Object factory){
        return factory instanceof ManagedResourceActivator && equals((ManagedResourceActivator<?>) factory);
    }

    static String getConnectorType(final ServiceReference<ManagedResourceConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(CONNECTOR_TYPE_IDENTITY_PROPERTY), ""):
                "";
    }

    static String getConnectionString(final ServiceReference<ManagedResourceConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(CONNECTOR_STRING_IDENTITY_PROPERTY), ""):
                "";
    }

    static String getManagedResourceName(final ServiceReference<ManagedResourceConnector<?>> connectorRef){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY), ""):
                "";
    }

    /**
     * Determines whether the specified bundle provides implementation of the SNAMP Management Connector.
     * @param bnd The bundle to check.
     * @return {@literal true}, if the specified bundle provides implementation of the management connector;
     *      otherwise, {@literal false}.
     */
    public static boolean isResourceConnectorBundle(final Bundle bnd){
        return bnd != null && bnd.getHeaders().get(CONNECTOR_NAME_MANIFEST_HEADER) != null;
    }

    private static List<Bundle> getResourceConnectorBundles(final BundleContext context){
        final Bundle[] bundles = context.getBundles();
        final List<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(isResourceConnectorBundle(bnd)) result.add(bnd);
        return result;
    }

    static List<Bundle> getResourceConnectorBundles(final BundleContext context, final String connectorName){
        final Bundle[] bundles = context.getBundles();
        final List<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(Objects.equals(bnd.getHeaders().get(CONNECTOR_NAME_MANIFEST_HEADER), connectorName))
                result.add(bnd);
        return result;
    }

    /**
     * Stops all bundles with management connectors.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws org.osgi.framework.BundleException Unable to stop management connectors.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void stopResourceConnectors(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceConnectorBundles(context))
            bnd.stop();
    }

    /**
     * Stops the specified management connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorName The name of the connector to stop.
     * @throws BundleException Unable to stop management connector.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static void stopResourceConnector(final BundleContext context, final String connectorName) throws BundleException {
        for(final Bundle bnd: getResourceConnectorBundles(context, connectorName))
            bnd.stop();
    }

    /**
     * Starts all bundles with management connectors.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws org.osgi.framework.BundleException Unable to start management connectors.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void startResourceConnectors(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceConnectorBundles(context))
            bnd.stop();
    }

    /**
     * Starts management connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorName The name of the connector to start.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start management connector.
     */
    public static void startResourceConnector(final BundleContext context, final String connectorName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceConnectorBundles(context, connectorName))
            bnd.start();
    }

    /**
     * Gets a collection of installed connectors (system names).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed connectors (system names).
     */
    public static Collection<String> getInstalledResourceConnectors(final BundleContext context){
        final Collection<Bundle> candidates = getResourceConnectorBundles(context);
        final Collection<String> systemNames = new ArrayList<>(candidates.size());
        for(final Bundle bnd: candidates)
            systemNames.add(bnd.getHeaders().get(CONNECTOR_NAME_MANIFEST_HEADER));
        return systemNames;
    }

    static String createFilter(final String connectorType, final String filter){
        return filter == null || filter.isEmpty() ?
                String.format("(%s=%s)", CONNECTOR_TYPE_IDENTITY_PROPERTY, connectorType):
                String.format("(&(%s=%s)%s)", CONNECTOR_TYPE_IDENTITY_PROPERTY, connectorType, filter);
    }

    static String createFilter(final String resourceName){
        return String.format("(%s=%s)", MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, resourceName);
    }

    static boolean isResourceConnector(final ServiceReference<?> ref){
        return Utils.isInstanceOf(ref, ManagedResourceConnector.class) &&
                ArrayUtils.contains(ref.getPropertyKeys(), CONNECTOR_TYPE_IDENTITY_PROPERTY) &&
                ArrayUtils.contains(ref.getPropertyKeys(), MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY);
    }
}