package com.itworks.snamp.connectors;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.configuration.*;
import com.itworks.snamp.connectors.discovery.AbstractDiscoveryService;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.core.OsgiLoggingContext;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.Internal;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.LicenseLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import org.osgi.framework.*;
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
public abstract class AbstractManagedResourceActivator<TConnector extends ManagedResourceConnector<?>> extends AbstractServiceLibrary {
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
    private static final String MGMT_MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY = "managedResource";
    private static final String CONNECTOR_STRING_IDENTITY_PROPERTY = "connectionString";
    private static final String CONNECTOR_TYPE_IDENTITY_PROPERTY = CONNECTOR_NAME_MANIFEST_HEADER;

    private static final ActivationProperty<CompliantResources> COMPLIANT_RESOURCES_HOLDER = defineActivationProperty(CompliantResources.class, CompliantResources.EMPTY);
    private static final ActivationProperty<String> CONNECTOR_NAME_HOLDER = defineActivationProperty(String.class);

    private static final class CompliantResources extends HashMap<String, ManagedResourceConfiguration>{

        private CompliantResources(){

        }

        public static final CompliantResources EMPTY = new CompliantResources();

        public CompliantResources(final String connectorName, final AgentConfiguration configuration){
            this(connectorName, configuration.getManagedResources());
        }

        public CompliantResources(final String connectorName, final Map<String, AgentConfiguration.ManagedResourceConfiguration> targets){
            super(targets.size());
            for(final Map.Entry<String, ManagedResourceConfiguration> entry: targets.entrySet())
                if(Objects.equals(connectorName, entry.getValue().getConnectionType()))
                    put(entry.getKey(), entry.getValue());
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
     * @see com.itworks.snamp.connectors.AbstractManagedResourceActivator.ConfigurationEntityDescriptionManager
     * @see com.itworks.snamp.connectors.AbstractManagedResourceActivator.DiscoveryServiceManager
     * @see com.itworks.snamp.connectors.AbstractManagedResourceActivator.LicensingDescriptionServiceManager
     */
    protected static abstract class SupportConnectorServiceManager<S extends FrameworkService, T extends S> extends ProvidedService<S, T>{

        private SupportConnectorServiceManager(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        /**
         * Gets name of the underlying resource connector.
         * @return The name of the underlying resource connector.
         */
        protected final String getConnectorName(){
            return getActivationPropertyValue(CONNECTOR_NAME_HOLDER);
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

        public ConnectorLicensingDescriptorService(final LicenseReader reader,
                                                   final Class<L> descriptor,
                                                   final Supplier<L> fallbackFactory){
            this.licenseReader = reader;
            this.descriptor = descriptor;
            this.fallbackFactory = fallbackFactory;
        }

        /**
         * Gets logger associated with this service.
         *
         * @return The logger associated with this service.
         */
        @Override
        public Logger getLogger() {
            return licenseReader.getLogger();
        }

        /**
         * Gets a read-only collection of license limitations.
         *
         * @return A read-only collection of license limitations.
         */
        @Override
        public Collection<String> getLimitations() {
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
        public String getDescription(final String limitationName, final Locale loc) {
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
                    fallbackFactory);
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
         * Gets logger associated with discovery service.
         * @return The logger associated with discovery service.
         */
        protected abstract Logger getLogger();

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
                    return SimpleDiscoveryServiceManager.this.getLogger();
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
        protected final void cleanupService(final AbstractDiscoveryService<TProvider> serviceInstance, final boolean stopBundle) throws Exception {
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
     * Represents a set of factories for resource connector related services.
     * @param <TConnectorImpl> Type of the resource connector implementation.
     * @author Roman Sakno
     * @since 1.0
     */
    protected static abstract class ServiceFactories<TConnectorImpl extends ManagedResourceConnector<?>> implements ProvidedServices{

        /**
         * Creates a new instance of the management connector factory.
         * @param resourceName The name of the managed resource.
         * @param instances Count of already instantiated connectors.
         * @param services A collection of resolved dependencies.
         * @param activationProperties A collection of activation properties to read.
         * @return A new instance of the resource connector factory.
         */
        protected abstract ManagedResourceConnectorManager<TConnectorImpl> createConnectorManager(final String resourceName,
                                                                                                  final long instances,
                                                                                                  final Iterable<RequiredService<?>> services,
                                                                                                  final ActivationPropertyReader activationProperties);

        /**
         * Creates a new factory for the special service that provides configuration schema for the resource connector.
         * <p>
         *     In the default implementation this method returns {@literal null}.
         * </p>
         * @param activationProperties A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         * @return A new factory for the special service that provides configuration schema for the resource connector.
         * @see com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider
         */
        @MethodStub
        protected ConfigurationEntityDescriptionManager<?> createDescriptionServiceManager(final ActivationPropertyReader activationProperties,
                                                                                           final RequiredService<?>... bundleLevelDependencies){
            return null;
        }

        /**
         * Creates a new instance of the {@link com.itworks.snamp.connectors.discovery.DiscoveryService} factory.
         * @param activationProperties A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         * @return A new factory of the special service that can automatically discover elements of the managed resource.
         * @see com.itworks.snamp.connectors.discovery.DiscoveryService
         */
        @MethodStub
        protected DiscoveryServiceManager<?> createDiscoveryServiceManager(final ActivationPropertyReader activationProperties,
                                                                           final RequiredService<?>... bundleLevelDependencies){
            return null;
        }

        @Internal
        @MethodStub
        protected LicensingDescriptionServiceManager createLicenseServiceManager(){
            return null;
        }

        /**
         * Creates a new maintenance service.
         * <p>
         *     In the default implementation this method returns {@literal null}.
         * </p>
         * @param dependencies A collection of resolved library-level dependencies.
         * @return A new instance of the service provider.
         */
        @MethodStub
        protected MaintenanceServiceManager<?> createMaintenanceServiceManager(final RequiredService<?>... dependencies){
            return null;
        }

        private void forEachAdditionalService(final SafeConsumer<ProvidedService<?, ?>> handler,
                                              final ActivationPropertyReader activationProperties,
                                              final RequiredService<?>... bundleLevelDependencies){
            ProvidedService<?, ?> advancedService = createDescriptionServiceManager(activationProperties, bundleLevelDependencies);
            if(advancedService != null) handler.accept(advancedService);
            advancedService = createDiscoveryServiceManager(activationProperties, bundleLevelDependencies);
            if(advancedService != null) handler.accept(advancedService);
            advancedService = createLicenseServiceManager();
            if(advancedService != null) handler.accept(advancedService);
            advancedService = createMaintenanceServiceManager(bundleLevelDependencies);
            if(advancedService != null) handler.accept(advancedService);
        }

        /**
         * Exposes all provided services via the input collection.
         *
         * @param services      A collection of provided services to fill.
         * @param activationProperties Activation properties to read.
         * @param bundleLevelDependencies A collection of bundle-level dependencies.
         */
        @Override
        public final void provide(final Collection<ProvidedService<?, ?>> services,
                                  final ActivationPropertyReader activationProperties,
                                  final RequiredService<?>... bundleLevelDependencies) {
            //iterates through each compliant target and instantiate manager for each resource connector
            final Map<String, ManagedResourceConfiguration> resources = activationProperties.getValue(COMPLIANT_RESOURCES_HOLDER);
            int instanceCount = 0;
            for(final String targetName: resources != null ? resources.keySet() : Collections.<String>emptySet()) {
                final ManagedResourceConnectorManager<TConnectorImpl> provider = createConnectorManager(targetName, instanceCount, Arrays.asList(bundleLevelDependencies), activationProperties);
                if(provider != null)
                    services.add(provider);
            }
            forEachAdditionalService(new SafeConsumer<ProvidedService<?, ?>>() {
                @Override
                public void accept(final ProvidedService<?, ?> service) {
                    services.add(service);
                }
            }, activationProperties, bundleLevelDependencies);
        }
    }

    /**
     * Represents factory for resource connector.
     * @param <TConnectorImpl> Type of the management connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected abstract static class ManagedResourceConnectorManager<TConnectorImpl extends ManagedResourceConnector<?>> extends ProvidedService<ManagedResourceConnector, TConnectorImpl>{
        /**
         * Represents name of the managed resource bounded to this resource connector factory.
         */
        protected final String managedResourceName;

        /**
         * Initializes a new management connector factory.
         * @param managedResource The name of the managed resource.
         * @param dependencies A collection of connector dependencies.
         * @throws IllegalArgumentException config is {@literal null}.
         */
        protected ManagedResourceConnectorManager(final String managedResource, final RequiredService<?>... dependencies) {
            super(ManagedResourceConnector.class, dependencies);
            this.managedResourceName = managedResource;
        }

        private AgentConfiguration.ManagedResourceConfiguration getConfiguration(){
            return Utils.getProperty(getActivationPropertyValue(COMPLIANT_RESOURCES_HOLDER),
                    managedResourceName,
                    AgentConfiguration.ManagedResourceConfiguration.class,
                    Suppliers.<AgentConfiguration.ManagedResourceConfiguration>ofInstance(null));
        }

        /**
         * Gets name of this connector.
         * @return The name of this connector.
         */
        protected final String getConnectorName(){
            return getActivationPropertyValue(CONNECTOR_NAME_HOLDER);
        }

        /**
         * Creates a new instance of the management connector.
         * @param connectionString The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        protected abstract TConnectorImpl createConnector(final String connectionString,
                                                          final Map<String, String> connectionOptions,
                                                          final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         * @throws Exception Failed to create management connector instance.
         */
        @Override
        protected final TConnectorImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception{
            final AgentConfiguration.ManagedResourceConfiguration config = getConfiguration();
            createIdentity(managedResourceName, getConfiguration(), identity);
            return createConnector(config.getConnectionString(), config.getParameters(), dependencies);
        }

        /**
         * Invokes {@link ManagedResourceConnector#close()} method on the instantiated management connector.
         * @param serviceInstance An instance of the hosted service to cleanup.
         * @param stopBundle      {@literal true}, if this method calls when the owner bundle is stopping;
         *                        {@literal false}, if this method calls when loosing dependency.
         */
        @Override
        protected void cleanupService(final TConnectorImpl serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
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

        public EventAdminTransport(final String connectorName,
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
     * Represents a base class for management connector factory which supports notifications.
     * @param <TConnectorImpl> Type of the management connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class NotificationSupportManager<TConnectorImpl extends ManagedResourceConnector<?> & NotificationSupport> extends ManagedResourceConnectorManager<TConnectorImpl> {
        private static final String NOTIF_TRANSPORT_LISTENER_ID = "EventAdminTransport";

        /**
         * Initializes a new factory for the management connector with notification support.
         *
         * @param targetName          The name of the management target.
         * @param publisherDependency Notification delivery channel dependency. This dependency is mandatory.
         * @param dependencies        A collection of connector dependencies.
         */
        protected NotificationSupportManager(final String targetName,
                                             final RequiredServiceAccessor<EventAdmin> publisherDependency, final RequiredService<?>... dependencies) {
            super(targetName, ObjectArrays.concat(dependencies, publisherDependency));
        }

        /**
         * Initializes a new factory for the management connector with notification support.
         * <p>
         * This constructor calls {@link #NotificationSupportManager(String, com.itworks.snamp.core.AbstractServiceLibrary.RequiredServiceAccessor, com.itworks.snamp.core.AbstractServiceLibrary.RequiredService[])}
         * and pass {@link com.itworks.snamp.core.AbstractBundleActivator.SimpleDependency} as dependency
         * descriptor for {@link org.osgi.service.event.EventAdmin} service.
         * </p>
         *
         * @param targetName   The name of the management target.
         * @param dependencies A collection of connector dependencies.
         */
        @SuppressWarnings("UnusedDeclaration")
        protected NotificationSupportManager(final String targetName,
                                             final RequiredService<?>... dependencies) {
            this(targetName, new SimpleDependency<>(EventAdmin.class), dependencies);
        }

        /**
         * Creates a new instance of the management connector that supports notifications.
         *
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        protected abstract TConnectorImpl newNotificationSupport(final String connectionString,
                                                                 final Map<String, String> connectionOptions,
                                                                 final RequiredService<?>... dependencies) throws Exception;

        /**
         * Creates a new instance of the management connector.
         *
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws Exception Failed to create management connector instance.
         */
        @Override
        protected TConnectorImpl createConnector(final String connectionString, final Map<String, String> connectionOptions, final RequiredService<?>... dependencies) throws Exception {
            @SuppressWarnings("unchecked")
            final RequiredServiceAccessor<EventAdmin> eventAdmin = findDependency(RequiredServiceAccessor.class, EventAdmin.class, dependencies);
            final TConnectorImpl connector = newNotificationSupport(connectionString, connectionOptions, dependencies);
            connector.subscribe(NOTIF_TRANSPORT_LISTENER_ID, new EventAdminTransport(getConnectorName(), managedResourceName, eventAdmin, connector), true);
            return connector;
        }
    }

    /**
     * Represents name of the management connector.
     */
    public final String connectorName;

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @param connectorFactory A factory that exposes collection of management connector factories.
     * @throws IllegalArgumentException connectorName is {@literal null}.
     */
    protected AbstractManagedResourceActivator(final String connectorName, final ServiceFactories<TConnector> connectorFactory){
        super(connectorFactory);
        this.connectorName = connectorName;
    }

    /**
     * Configures management connector identity.
     * @param resourceName The name of the management target.
     * @param config The management target configuration used to create identity.
     * @param identity The identity map to fill.
     */
    public static void createIdentity(final String resourceName,
                                      final ManagedResourceConfiguration config,
                                      final Map<String, Object> identity){
        identity.put(MGMT_MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, resourceName);
        identity.put(CONNECTOR_TYPE_IDENTITY_PROPERTY, config.getConnectionType());
        identity.put(CONNECTOR_STRING_IDENTITY_PROPERTY, config.getConnectionString());
        identity.put(Constants.SERVICE_PID, PersistentConfigurationManager.getResourcePersistentID(resourceName));
        for(final Map.Entry<String, String> option: config.getParameters().entrySet())
            identity.put(option.getKey(), option.getValue());
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
     * @throws Exception An error occurred during bundle initialization.
     */
    @Override
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationManager.class));
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
        final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
        activationProperties.publish(COMPLIANT_RESOURCES_HOLDER, new CompliantResources(connectorName, configManager.getCurrentConfiguration()));
        activationProperties.publish(CONNECTOR_NAME_HOLDER, connectorName);
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(connectorName);
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
        try (final OsgiLoggingContext context = getLoggingContext()) {
            context.log(Level.SEVERE, String.format("Unable to instantiate %s connector", connectorName), e);
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
        try (final OsgiLoggingContext context = getLoggingContext()) {
            context.log(Level.SEVERE, String.format("Unable to release %s connector instance", connectorName), e);
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

    }

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return connectorName;
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    public final boolean equals(final AbstractManagedResourceActivator<?> factory){
        return factory != null && connectorName.equals(factory.connectorName);
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
        return factory instanceof AbstractManagedResourceActivator && equals((AbstractManagedResourceActivator<?>) factory);
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
                Objects.toString(connectorRef.getProperty(MGMT_MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY), ""):
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
}
