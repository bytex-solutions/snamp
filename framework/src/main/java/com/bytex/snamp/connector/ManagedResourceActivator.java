package com.bytex.snamp.connector;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.configuration.ConfigParameters;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.connector.discovery.DiscoveryService;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.management.Maintainable;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.openmbean.CompositeData;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.*;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a base class for management connector bundle.
 * <p>
 *     This bundle activator represents a factory of {@link ManagedResourceConnector} implementations.
 *     Each connector should be registered as separated service in OSGi environment.
 * </p>
 * @param <TConnector> Type of the management connector.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public class ManagedResourceActivator<TConnector extends ManagedResourceConnector> extends AbstractServiceLibrary {

    private static final String MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY = "managedResource";
    private static final String CONNECTOR_STRING_IDENTITY_PROPERTY = "connectionString";
    private static final String CONNECTION_TYPE_IDENTITY_PROPERTY = "connectionType";

    private static final ActivationProperty<String> CONNECTOR_TYPE_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents an interface responsible for lifecycle control over resource connector instances.
     * @param <TConnector> Type of the managed resource connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
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
                                                              final Map<String, ? extends F> features) throws Exception;
    }

    /**
     * Represents managed resource connector factory.
     * <p>
     *     This class provides the default implementation of {@link ManagedResourceActivator.ManagedResourceConnectorLifecycleController#updateConnector(ManagedResourceConnector, String, String, java.util.Map, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])}
     *     in the following manner:
     *     <ul>
     *         <li>{@code updateConnector} - sequentially calls {@link ManagedResourceConnector#close()} on the existing connector
     *         and creates a new instance of the connector using {@code createConnector}.</li>
     *     </ul>
     *     In the derived factory you may implements just {@link ManagedResourceActivator.ManagedResourceConnectorLifecycleController#createConnector(String, String, java.util.Map, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])}
     *     method, but other methods are available for overriding.
     * </p>
     * @param <TConnector> Type of the connector implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
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
                connector.close();
                connector = createConnector(resourceName,
                        connectionString,
                        connectionParameters,
                        dependencies);
            }
            return connector;
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
     * @version 2.0
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
         * @param attributeName The name of the attribute in the managed resource.
         * @param readWriteTimeout The attribute read/write timeout.
         * @param options The attribute configuration options.
         * @return {@literal true}, if attribute registered successfully; otherwise, {@literal false}.
         */
        protected abstract boolean addAttribute(final TConnector connector,
                                             final String attributeName,
                                             final Duration readWriteTimeout,
                                             final CompositeData options);

        /**
         * Removes all attributes except specified in the collection.
         * @param connector The connector to modify.
         * @param attributes A set of attributes which should not be deleted.
         */
        protected abstract void retainAttributes(final TConnector connector,
                                                 final Set<String> attributes);

        private static boolean setFeatureNameIfNecessary(final FeatureConfiguration feature,
                                                         final String name){
            if(feature.getParameters().containsKey(FeatureConfiguration.NAME_KEY))
                return false;
            else {
                feature.getParameters().put(FeatureConfiguration.NAME_KEY, name);
                return true;
            }
        }

        private void updateAttributes(final TConnector connector,
                                      final Map<String, ? extends AttributeConfiguration> attributes){
            final Set<String> addedAttributes = Sets.newHashSetWithExpectedSize(attributes.size());
            for(final Map.Entry<String, ? extends AttributeConfiguration> attr: attributes.entrySet()) {
                final String attributeName = attr.getKey();
                final AttributeConfiguration config = attr.getValue();
                setFeatureNameIfNecessary(config, attributeName);
                if (addAttribute(connector,
                        attributeName,
                        config.getReadWriteTimeout(),
                        new ConfigParameters(config)))
                    addedAttributes.add(attributeName);
            }
            retainAttributes(connector, addedAttributes);
        }

        /**
         * Enables managed resource notification.
         * <p>
         *     When implementing this method you must take into
         *     account already existed notifications in the managed resource connector.
         *     If notification is enabled in the managed resource connector then
         *     it should re-enable the notification (disable and then enable again).
         * @param connector The managed resource connector.
         * @param category The notification category.
         * @param options The notification configuration options.
         * @return {@literal true}, if the specified notification is enabled; otherwise, {@literal false}.
         */
        protected abstract boolean enableNotifications(final TConnector connector,
                                                    final String category,
                                                    final CompositeData options);

        /**
         * Disables all notifications except specified in the collection.
         * @param connector The connector to modify.
         * @param events A set of subscription lists which should not be disabled.
         */
        protected abstract void retainNotifications(final TConnector connector,
                                                    final Set<String> events);

        private void updateEvents(final TConnector connector,
                                  final Map<String, ? extends EventConfiguration> events){
            final Set<String> enabledEvents = Sets.newHashSetWithExpectedSize(events.size());
            for(final Map.Entry<String, ? extends EventConfiguration> event: events.entrySet()){
                final String category = event.getKey();
                final EventConfiguration config = event.getValue();
                setFeatureNameIfNecessary(config, category);
                if(enableNotifications(connector,
                        category,
                        new ConfigParameters(config)))
                    enabledEvents.add(category);
            }
            retainNotifications(connector, enabledEvents);
        }

        protected abstract boolean enableOperation(final TConnector connector,
                                                final String operationName,
                                                final Duration invocationTimeout,
                                                final CompositeData options);

        /**
         * Disables all operations except specified in the collection.
         * @param connector The connector to modify.
         * @param operations A set of operations which should not be disabled.
         */
        protected abstract void retainOperations(final TConnector connector,
                                                 final Set<String> operations);

        private void updateOperations(final TConnector connector,
                                      final Map<String, ? extends OperationConfiguration> operations){
            final Set<String> enabledOperations = Sets.newHashSetWithExpectedSize(operations.size());
            for(final Map.Entry<String, ? extends OperationConfiguration> op: operations.entrySet()){
                final String operationName = op.getKey();
                final OperationConfiguration config = op.getValue();
                setFeatureNameIfNecessary(config, operationName);
                if(enableOperation(connector,
                        operationName,
                        config.getInvocationTimeout(),
                        new ConfigParameters(config)))
                    enabledOperations.add(operationName);
            }
            retainOperations(connector, enabledOperations);
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
        public final <F extends FeatureConfiguration> void updateConnector(final TConnector connector, final Class<F> featureType, final Map<String, ? extends F> features) throws Exception {
            if(Objects.equals(featureType, AttributeConfiguration.class))
                updateAttributes(connector, (Map<String, ? extends AttributeConfiguration>)features);
            else if(Objects.equals(featureType, EventConfiguration.class))
                updateEvents(connector, (Map<String, ? extends EventConfiguration>)features);
            else if(Objects.equals(featureType, OperationConfiguration.class))
                updateOperations(connector, (Map<String, ? extends OperationConfiguration>)features);
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
            super(ManagedResourceConnector.class, ObjectArrays.<RequiredService>concat(dependencies, new SimpleDependency<>(ConfigurationManager.class)));
            this.controller = Objects.requireNonNull(controller, "controller is null.");
            this.connectorType = connectorType;
            this.configurationHashes = Maps.newHashMapWithExpectedSize(10);
        }

        private ManagedResourceConnectorRegistry(final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                                 final RequiredService<?>... dependencies){
            this(ManagedResourceConnector.getResourceConnectorType(Utils.getBundleContextOfObject(controller).getBundle()), controller, dependencies);
        }

        @SuppressWarnings("unchecked")
        private static CMManagedResourceParser getParser(final RequiredService<?>... dependencies){
            final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
            assert configManager != null;
            final CMManagedResourceParser parser = configManager.queryObject(CMManagedResourceParser.class);
            assert parser != null;
            return parser;
        }

        @Override
        protected String getFactoryPID(final RequiredService<?>[] dependencies) {
            return getParser(dependencies).getFactoryPersistentID(connectorType);
        }

        private void updateFeatures(final TConnector connector,
                            final Dictionary<String, ?> configuration,
                                    final CMManagedResourceParser parser) throws Exception {
            controller.updateConnector(connector,
                    AttributeConfiguration.class,
                    parser.getAttributes(configuration));
            controller.updateConnector(connector,
                    EventConfiguration.class,
                    parser.getEvents(configuration));
            controller.updateConnector(connector,
                    OperationConfiguration.class,
                    parser.getOperations(configuration));
            //expansion should be the last instruction in this method because updating procedure
            //may remove all automatically added attributes
            AbstractManagedResourceConnector.expandAll(connector);
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
            final CMManagedResourceParser parser = getParser(dependencies);
            final String resourceName = parser.getResourceName(configuration);
            final BigInteger oldHash = configurationHashes.get(resourceName);
            final String connectionString = parser.getConnectionString(configuration);
            final Map<String, String> connectorParameters = parser.getParameters(configuration);
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
            updateFeatures(connector, configuration, parser);
            return connector;
        }

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])} failed.
         * @param logger
         * @param servicePID    The persistent identifier associated with the service.
         * @param configuration The configuration of the service.
         * @param e             An exception occurred when updating service.
         */
        @Override
        protected void failedToUpdateService(final Logger logger,
                                             final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to update connector '%s'", servicePID), e);
        }

        /**
         * Logs error details when {@link #dispose(Object, boolean)} failed.
         * @param logger
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e          An exception occurred when disposing service.
         */
        @Override
        protected void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to dispose connector '%s'", servicePID), e);
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
            final CMManagedResourceParser parser = getParser(dependencies);
            final String resourceName = parser.getResourceName(configuration);
            final Map<String, String> options = parser.getParameters(configuration);
            final String connectionString = parser.getConnectionString(configuration);
            configurationHashes.put(resourceName, computeConnectionParamsHashCode(connectionString, options));
            createIdentity(resourceName,
                    connectorType,
                    connectionString,
                    identity);
            final TConnector result = controller.createConnector(resourceName, connectionString, options, dependencies);
            updateFeatures(result, configuration, parser);
            return result;
        }

        @Override
        protected void cleanupService(final TConnector service,
                                      final Dictionary<String, ?> identity) throws Exception {
            try {
                service.close();
            }
            finally {
                configurationHashes.remove(getManagedResourceName(identity));
            }
        }
    }

    /**
     * Represents superclass for all-optional resource connector service providers.
     * You cannot derive from this class directly.
     * @param <S> Type of the gateway-related service contract.
     * @param <T> Type of the gateway-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     * @see #maintenanceService(Supplier)
     * @see #discoveryService(Function, RequiredService[])
     * @see #configurationDescriptor(Supplier)
     */
    protected static abstract class SupportConnectorServiceManager<S extends FrameworkService, T extends S> extends ProvidedService<S, T> {

        private SupportConnectorServiceManager(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        abstract T activateService(final RequiredService<?>... dependencies) throws Exception;

        @Override
        protected final T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(CONNECTION_TYPE_IDENTITY_PROPERTY, getConnectorName());
            return activateService(dependencies);
        }

        /**
         * Gets name of the underlying resource connector.
         * <p>
         *     This property is available when this manager is in {@link com.bytex.snamp.core.AbstractBundleActivator.ActivationState#ACTIVATED}
         *     state only.
         * </p>
         * @return The name of the underlying resource connector.
         * @see #getState()
         */
        private String getConnectorName() {
            return getActivationPropertyValue(CONNECTOR_TYPE_HOLDER);
        }

        /**
         * Gets logger associated with the managed resource connector.
         * <p>
         *     This property is available when this manager is in {@link com.bytex.snamp.core.AbstractBundleActivator.ActivationState#ACTIVATED}
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
     * Initializes a new connector factory.
     * @param controller Resource connector lifecycle controller. Cannot be {@literal null}.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                       final SupportConnectorServiceManager<?, ?>... optionalServices) {
        this(controller,
                emptyArray(RequiredService[].class),
                optionalServices);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportConnectorServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final Supplier<T> factory){
        return new SupportConnectorServiceManager<ConfigurationEntityDescriptionProvider, T>(ConfigurationEntityDescriptionProvider.class) {
            @Override
            T activateService(final RequiredService<?>... dependencies) {
                return factory.get();
            }
        };
    }

    protected static <T extends Maintainable> SupportConnectorServiceManager<Maintainable,T> maintenanceService(final Supplier<T> factory) {
        return new SupportConnectorServiceManager<Maintainable, T>(Maintainable.class) {
            @Override
            T activateService(RequiredService<?>... dependencies) {
                return factory.get();
            }
        };
    }

    protected static <T extends DiscoveryService> SupportConnectorServiceManager<DiscoveryService, T> discoveryService(final Function<RequiredService<?>[], T> factory, final RequiredService<?>... dependencies) {
        return new SupportConnectorServiceManager<DiscoveryService, T>(DiscoveryService.class, dependencies) {
            @Override
            T activateService(final RequiredService<?>... dependencies) {
                return factory.apply(dependencies);
            }
        };
    }

    /**
     * Initializes a new connector factory.
     * @param controller Resource connector lifecycle controller. Cannot be {@literal null}.
     * @param connectorDependencies A collection of connector-level dependencies.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceConnectorLifecycleController<TConnector> controller,
                                       final RequiredService<?>[] connectorDependencies,
                                       final SupportConnectorServiceManager<?, ?>[] optionalServices){
        super(ObjectArrays.concat(optionalServices, new ServiceSubRegistryManager<?, ?>[]{ new ManagedResourceConnectorRegistry<>(controller, connectorDependencies)}, ProvidedService.class));
    }

    private static void createIdentity(final String resourceName,
                                       final String connectorType,
                                       final String connectionString,
                                       final Map<String, Object> identity){
        identity.put(MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, resourceName);
        identity.put(CONNECTION_TYPE_IDENTITY_PROPERTY, connectorType);
        identity.put(CONNECTOR_STRING_IDENTITY_PROPERTY, connectionString);
    }

    /**
     * Gets type of the connector.
     * @return Type of the connector.
     */
    public final String getConnectorType(){
        return ManagedResourceConnector.getResourceConnectorType(Utils.getBundleContextOfObject(this).getBundle());
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
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
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
        activationProperties.publish(CONNECTOR_TYPE_HOLDER, getConnectorType());
        getLogger().log(Level.INFO, String.format("Activating resource connector of type %s", getConnectorType()));
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    @Override
    protected Logger getLogger(){
        return AbstractManagedResourceConnector.getLogger(getConnectorType());
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.bytex.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        getLogger().log(Level.SEVERE, String.format("Unable to instantiate %s connector",
                getConnectorType()), e);
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
        getLogger().log(Level.SEVERE, String.format("Unable to release %s connector instance",
                getConnectorType()), e);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected final void deactivate(final ActivationPropertyReader activationProperties) {
        getLogger().log(Level.INFO, String.format("Unloading connector of type %s", getConnectorType()));
    }

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return getConnectorType();
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP resource connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP resource connector; otherwise, {@literal false}.
     */
    public final boolean equals(final ManagedResourceActivator<?> factory){
        return factory != null && Objects.equals(getConnectorType(), factory.getConnectorType());
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP resource connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP resource connector; otherwise, {@literal false}.
     */
    @Override
    public final boolean equals(final Object factory){
        return factory instanceof ManagedResourceActivator && equals((ManagedResourceActivator<?>) factory);
    }

    @Override
    public final int hashCode() {
        return getConnectorType().hashCode();
    }

    static String getManagedResourceName(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return connectorRef != null ?
                getManagedResourceName(getProperties(connectorRef)) :
                "";
    }

    static String getConnectionString(final ServiceReference<ManagedResourceConnector> identity){
        return Utils.getProperty(getProperties(identity), CONNECTOR_STRING_IDENTITY_PROPERTY, String.class, "");
    }

    private static String getManagedResourceName(final Dictionary<String, ?> identity){
        return Utils.getProperty(identity, MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, String.class, "");
    }

    private static List<Bundle> getResourceConnectorBundles(final BundleContext context) {
        return Arrays.stream(context.getBundles())
                .filter(ManagedResourceConnector::isResourceConnectorBundle)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    static List<Bundle> getResourceConnectorBundles(final BundleContext context, final String connectorName){
        return Arrays.stream(context.getBundles())
                .filter(bnd -> ManagedResourceConnector.getResourceConnectorType(bnd).equals(connectorName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Stops all bundles with resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return Number of stopped bundles.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws org.osgi.framework.BundleException Unable to stop resource connector.
     */
    public static int disableConnectors(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        int count = 0;
        for(final Bundle bnd: getResourceConnectorBundles(context)) {
            bnd.stop();
            count += 1;
        }
        return count;
    }

    /**
     * Stops the specified resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorName The name of the connector to stop.
     * @return {@literal true}, if bundle with the specified connector exist; otherwise, {@literal false}.
     * @throws BundleException Unable to stop resource connector.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static boolean disableConnector(final BundleContext context, final String connectorName) throws BundleException {
        boolean success = false;
        for(final Bundle bnd: getResourceConnectorBundles(context, connectorName)) {
            bnd.stop();
            success = true;
        }
        return success;
    }

    /**
     * Starts all bundles with resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return Number of started resource connector.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws org.osgi.framework.BundleException Unable to start resource connector.
     */
    public static int enableConnectors(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        int count = 0;
        for(final Bundle bnd: getResourceConnectorBundles(context)) {
            bnd.stop();
            count += 1;
        }
        return count;
    }

    /**
     * Starts resource connector.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param connectorName The name of the connector to start.
     * @return {@literal true}, if bundle with the specified connector exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start resource connector.
     */
    public static boolean enableConnector(final BundleContext context, final String connectorName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getResourceConnectorBundles(context, connectorName)) {
            bnd.start();
            success = true;
        }
        return success;
    }

    /**
     * Gets a collection of installed connector (system names).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed connector (system names).
     */
    public static Collection<String> getInstalledResourceConnectors(final BundleContext context){
        final Collection<Bundle> candidates = getResourceConnectorBundles(context);
        return candidates.stream()
                .filter(ManagedResourceConnector::isResourceConnectorBundle)
                .map(ManagedResourceConnector::getResourceConnectorType)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    static String createFilter(final String connectorType, final String filter){
        return isNullOrEmpty(filter) ?
                String.format("(%s=%s)", CONNECTION_TYPE_IDENTITY_PROPERTY, connectorType):
                String.format("(&(%s=%s)%s)", CONNECTION_TYPE_IDENTITY_PROPERTY, connectorType, filter);
    }

    static String createFilter(final String resourceName){
        return String.format("(%s=%s)", MANAGED_RESOURCE_NAME_IDENTITY_PROPERTY, resourceName);
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
    static BigInteger computeConnectionParamsHashCode(final String connectionString,
                                                      final Map<String, String> connectionParameters) {
        BigInteger result = toBigInteger(connectionString);
        for(final Map.Entry<String, String> entry: connectionParameters.entrySet()){
            result = result.xor(toBigInteger(entry.getKey()));
            result = result.xor(toBigInteger(entry.getValue()));
        }
        return result;
    }
}