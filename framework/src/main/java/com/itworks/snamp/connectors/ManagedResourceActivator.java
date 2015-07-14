package com.itworks.snamp.connectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.*;
import com.itworks.snamp.connectors.discovery.AbstractDiscoveryService;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.core.*;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.io.IOUtils;
import com.itworks.snamp.management.Maintainable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.CompositeData;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

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
public class ManagedResourceActivator<TConnector extends ManagedResourceConnector> extends AbstractServiceLibrary {
    /**
     * Describes missing prerequisite.
     */
    public static abstract class PrerequisiteException extends Exception{
        private static final long serialVersionUID = 7774921412080568085L;

        protected PrerequisiteException(final String message){
            super(message);
        }

        protected PrerequisiteException(final String message,
                                        final Exception cause){
            super(message, cause);
        }

        /**
         * Determines whether the bundle activation process should be terminated.
         * @return {@literal true} to terminate activator; otherwise, {@literal false}.
         */
        protected abstract boolean abortStarting();
    }

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
    private static final ActivationProperty<Boolean> PREREQUISITES_CHECK_HOLDER = defineActivationProperty(Boolean.class, false);

    /**
     * Represents an interface responsible for lifecycle control over resource connector instances.
     * @param <TConnector> Type of the managed resource connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface ManagedResourceConnectorLifecycleController<TConnector extends ManagedResourceConnector>{

        /**
         * Creates a new instance of the managed resource connector.
         * @param resourceName The name of the managed resource.
         * @param connectionString Managed resource connection string.
         * @param connectionParameters Connection parameters.
         * @param dependencies A collection of connector dependencies.
         * @return A new instance of the resource connector.
         * @throws Exception Unable to instantiate managed resource connector.
         */
        TConnector createConnector(final String resourceName,
                                   final String connectionString,
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
         * Updates features of the managed resource connector.
         * @param connector The instance of the connector.
         * @param featureType Type of the features in the collection.
         * @param features A collection of features configuration.
         * @throws Exception Unable to update managed resource features.
         */
        <F extends FeatureConfiguration> void updateConnector(final TConnector connector,
                                                              final Class<F> featureType,
                                                              final Map<String, F> features) throws Exception;

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
    protected static abstract class ManagedResourceConnectorFactory<TConnector extends ManagedResourceConnector> implements ManagedResourceConnectorLifecycleController<TConnector>{

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
            //trying to update resource connector on-the-fly
            try {
                connector.update(connectionString, connectionParameters);
            }
            catch (final ManagedResourceConnector.UnsupportedUpdateOperationException ignored){
                //Update operation is not supported -> force recreation
                releaseConnector(connector);
                connector = createConnector(resourceName,
                        connectionString,
                        connectionParameters,
                        dependencies);
            }
            return connector;
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

    /**
     * Represents simple managed resource modeler.
     * <p>
     *     Simple modeler supports modeling of attributes and notifications.
     *     When model of the managed resource was changed then modeler performs
     *     the following actions:
     *     <li>
     *         <ul>Removes all attributes from the connector and add each
     *         new attribute into the connector.</ul>
     *         <ul>Removes all notifications from the connector and add
     *         each new notification into the connector</ul>
     *     </li>
     * @param <TConnector> Type of the managed resource connector.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ManagedResourceConnectorModeler<TConnector extends ManagedResourceConnector> extends ManagedResourceConnectorFactory<TConnector>{
        /**
         * Registers a new attribute in the managed resource connector.
         * <p>
         *     When implementing this method you must take into
         *     account already existed attributes in the managed resource connector.
         *     If attribute exists in the managed resource connector then
         *     it should re-register an attribute.
         * @param connector The connector to modify.
         * @param attributeID The attribute identifier.
         * @param attributeName The name of the attribute in the managed resource.
         * @param readWriteTimeout The attribute read/write timeout.
         * @param options The attribute configuration options.
         */
        protected abstract void addAttribute(final TConnector connector,
                                             final String attributeID,
                                             final String attributeName,
                                             final TimeSpan readWriteTimeout,
                                             final CompositeData options);

        private void updateAttributes(final TConnector connector,
                                      final Map<String, AttributeConfiguration> attributes){
            for(final Map.Entry<String, AttributeConfiguration> attr: attributes.entrySet()){
                final String attributeID = attr.getKey();
                final AttributeConfiguration config = attr.getValue();
                addAttribute(connector, attributeID,
                        config.getAttributeName(),
                        config.getReadWriteTimeout(),
                        new ConfigParameters(config));
            }
        }

        /**
         * Enables managed resource notification.
         * <p>
         *     When implementing this method you must take into
         *     account already existed notifications in the managed resource connector.
         *     If notification is enabled in the managed resource connector then
         *     it should re-enable the notification (disable and then enable again).
         * @param connector The managed resource connector.
         * @param listId The notification subscription identifier.
         * @param category The notification category.
         * @param options The notification configuration options.
         */
        protected abstract void enableNotifications(final TConnector connector,
                                                    final String listId,
                                                    final String category,
                                                    final CompositeData options);

        private void updateEvents(final TConnector connector,
                                  final Map<String, EventConfiguration> events){
            for(final Map.Entry<String, EventConfiguration> event: events.entrySet()){
                final String listID = event.getKey();
                final EventConfiguration config = event.getValue();
                enableNotifications(connector,
                        listID,
                        config.getCategory(),
                        new ConfigParameters(config));
            }
        }

        protected abstract void enableOperation(final TConnector connector,
                                                final String operationID,
                                                final String operationName,
                                                final CompositeData options);

        private void updateOperations(final TConnector connector,
                                      final Map<String, OperationConfiguration> operations){
            for(final Map.Entry<String, OperationConfiguration> op: operations.entrySet()){
                final String operationID = op.getKey();
                final OperationConfiguration config = op.getValue();
                enableOperation(connector, operationID, config.getOperationName(), new ConfigParameters(config));
            }
        }

        /**
         * Updates features of the managed resource connector.
         *
         * @param connector   The instance of the connector.
         * @param featureType Type of the features in the collection.
         * @param features    A collection of features configuration.
         * @throws Exception Unable to update managed resource features.
         */
        @SuppressWarnings("unchecked")
        @Override
        public final <F extends FeatureConfiguration> void updateConnector(final TConnector connector, final Class<F> featureType, final Map<String, F> features) throws Exception {
            if(Objects.equals(featureType, AttributeConfiguration.class))
                updateAttributes(connector, (Map<String, AttributeConfiguration>)features);
            else if(Objects.equals(featureType, EventConfiguration.class))
                updateEvents(connector, (Map<String, EventConfiguration>)features);
            else if(Objects.equals(featureType, OperationConfiguration.class))
                updateOperations(connector, (Map<String, OperationConfiguration>)features);
        }
    }

    private static final class ManagedResourceConnectorRegistry<TConnector extends ManagedResourceConnector> extends ServiceSubRegistryManager<ManagedResourceConnector, TConnector> {
        private final ManagedResourceConnectorLifecycleController<TConnector> controller;
        private final Map<String, BigInteger> configurationHashes;

        /**
         * Represents name of the managed resource connector.
         */
        protected final String connectorType;

        private ManagedResourceConnectorRegistry(final String connectorType,
                                                 final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                                 final RequiredService<?>... dependencies) {
            super(ManagedResourceConnector.class,
                    PersistentConfigurationManager.getConnectorFactoryPersistentID(connectorType),
                    ArrayUtils.addToEnd(dependencies, new SimpleDependency<>(EventAdmin.class), RequiredService.class));
            this.controller = Objects.requireNonNull(controller, "controller is null.");
            this.connectorType = connectorType;
            this.configurationHashes = Maps.newHashMapWithExpectedSize(10);
        }

        @Override
        protected boolean isActivationAllowed() {
            return getActivationPropertyValue(PREREQUISITES_CHECK_HOLDER);
        }

        private OSGiLoggingContext getLoggingContext() {
            Logger logger = getActivationPropertyValue(LOGGER_HOLDER);
            if (logger == null)
                logger = AbstractManagedResourceConnector.getLogger(connectorType);
            return OSGiLoggingContext.get(logger,
                    Utils.getBundleContextByObject(controller));
        }

        private void updateFeatures(final TConnector connector,
                            final Dictionary<String, ?> configuration) throws Exception {
            AbstractManagedResourceConnector.expandAll(connector);
            controller.updateConnector(connector,
                    AttributeConfiguration.class,
                    PersistentConfigurationManager.getAttributes(configuration));
            controller.updateConnector(connector,
                    EventConfiguration.class,
                    PersistentConfigurationManager.getEvents(configuration));
            controller.updateConnector(connector,
                    OperationConfiguration.class,
                    PersistentConfigurationManager.getOperations(configuration));
        }



        /**
         * Updates the service with a new configuration.
         *
         * @param connector  The service to update.
         * @param configuration A new configuration of the service.
         * @return The updated service.
         * @throws Exception                                  Unable to update service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @Override
        protected TConnector update(TConnector connector,
                                    final Dictionary<String, ?> configuration,
                                    final RequiredService<?>... dependencies) throws Exception {
            final String resourceName = PersistentConfigurationManager.getResourceName(configuration);
            final BigInteger oldHash = configurationHashes.get(resourceName);
            final String connectionString = PersistentConfigurationManager.getConnectionString(configuration);
            final Map<String, String> connectorParameters = PersistentConfigurationManager.getResourceConnectorParameters(configuration);
            //we should not update resource connector if connection parameters was not changed
            final BigInteger newHash = computeConnectionParamsHashCode(connectionString, connectorParameters);
            if(!newHash.equals(oldHash)){
                configurationHashes.put(resourceName, newHash);
                connector = controller.updateConnector(connector,
                        resourceName,
                        connectionString,
                        connectorParameters,
                        dependencies);
            }
            //but we should always update resource features
            updateFeatures(connector, configuration);
            return connector;
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
            try (final OSGiLoggingContext logger = getLoggingContext()) {
                logger.log(Level.SEVERE, String.format("Unable to update connector. Connection string: %s, connection parameters: %s. Context: %s",
                                PersistentConfigurationManager.getConnectionString(configuration),
                                PersistentConfigurationManager.getResourceConnectorParameters(configuration),
                                LogicalOperation.current()),
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
            try (final OSGiLoggingContext logger = getLoggingContext()) {
                logger.log(Level.SEVERE, String.format("Unable to dispose connector. Context: %s",
                        LogicalOperation.current()), e);
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
            configurationHashes.put(resourceName, computeConnectionParamsHashCode(connectionString, options));
            createIdentity(resourceName,
                    connectorType,
                    connectionString,
                    options,
                    identity);
            final TConnector result = controller.createConnector(resourceName, connectionString, options, dependencies);
            updateFeatures(result, configuration);
            return result;
        }

        @Override
        protected void cleanupService(final TConnector service,
                                      final Dictionary<String, ?> identity) throws Exception {
            try {
                controller.releaseConnector(service);
            }
            finally {
                configurationHashes.remove(getManagedResourceName(identity));
            }
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
     */
    protected static abstract class SupportConnectorServiceManager<S extends FrameworkService, T extends S> extends ProvidedService<S, T> {

        private SupportConnectorServiceManager(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        protected final boolean isPrerequisitesOK(){
            return getActivationPropertyValue(PREREQUISITES_CHECK_HOLDER);
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
        protected abstract <T extends FeatureConfiguration> Collection<T> getManagementInformation(final Class<T> entityType,
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
                protected <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType, final TProvider provider) throws Exception {
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
     * on provided array of descriptions for each {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration}.
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
     * Represents name of the management connector.
     */
    public final String connectorType;
    private boolean prerequisitesOK;

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
                EMPTY_REQUIRED_SERVICES,
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
        this.prerequisitesOK = false;
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

    protected void checkPrerequisites() throws PrerequisiteException{
        prerequisitesOK = true;
    }

    /**
     * Initializes the library.
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     */
    @Override
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        prerequisitesOK = true;
        try {
            checkPrerequisites();
        } catch (final PrerequisiteException e) {
            if (e.abortStarting()) throw e;
            else try (final OSGiLoggingContext logger = getLoggingContext()) {
                logger.log(Level.WARNING, String.format("Preconditions for %s connector are not met", connectorType), e);
            }
            finally {
                prerequisitesOK = false;
            }
        }
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
        activationProperties.publish(PREREQUISITES_CHECK_HOLDER, prerequisitesOK);
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Activating resource connectors of type %s. Context: %s",
                    connectorType,
                    LogicalOperation.current()));
        }
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(connectorType);
    }

    private OSGiLoggingContext getLoggingContext(){
        return OSGiLoggingContext.get(getLogger(), Utils.getBundleContextByObject(this));
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.itworks.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        try (final OSGiLoggingContext logger = getLoggingContext()) {
            logger.log(Level.SEVERE, String.format("Unable to instantiate %s connector. Context: %s",
                    connectorType,
                    LogicalOperation.current()), e);
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
        try (final OSGiLoggingContext logger = getLoggingContext()) {
            logger.log(Level.SEVERE, String.format("Unable to release %s connector instance. Context: %s",
                    connectorType,
                    LogicalOperation.current()), e);
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
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Unloading connectors of type %s. Context: %s",
                    connectorType,
                    LogicalOperation.current()));
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

    @Override
    public final int hashCode() {
        return connectorType.hashCode();
    }

    static String getConnectorType(final ServiceReference<ManagedResourceConnector> connectorRef){
        return connectorRef != null ?
                getConnectorType(getProperties(connectorRef)):
                "";
    }

    static String getConnectionString(final ServiceReference<ManagedResourceConnector> connectorRef){
        return connectorRef != null ?
                getConnectionString(getProperties(connectorRef)):
                "";
    }

    static String getManagedResourceName(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return connectorRef != null ?
                getManagedResourceName(getProperties(connectorRef)) :
                "";
    }

    private static String getConnectorType(final Dictionary<String, ?> identity){
        return Utils.getProperty(identity, CONNECTOR_TYPE_IDENTITY_PROPERTY, String.class, "");
    }

    private static String getConnectionString(final Dictionary<String, ?> identity){
        return Utils.getProperty(identity, CONNECTOR_STRING_IDENTITY_PROPERTY, String.class, "");
    }

    private static String getManagedResourceName(final Dictionary<String, ?> identity){
        return Utils.getProperty(identity, MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, String.class, "");
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

    private static BigInteger toBigInteger(final String value){
        return value == null || value.isEmpty() ?
                BigInteger.ZERO :
                new BigInteger(value.getBytes(IOUtils.DEFAULT_CHARSET));
    }

    /**
     * Computes unique hash code for the specified connection parameters.
     * @param connectionString The managed resource connection string.
     * @param connectionParameters The managed resource connection parameters.
     * @return A unique hash code generated from connection string and connection parameters.
     */
    public static BigInteger computeConnectionParamsHashCode(final String connectionString,
                                                             final Map<String, String> connectionParameters) {
        BigInteger result = toBigInteger(connectionString);
        for(final Map.Entry<String, String> entry: connectionParameters.entrySet()){
            result = result.xor(toBigInteger(entry.getKey()));
            result = result.xor(toBigInteger(entry.getValue()));
        }
        return result;
    }
}