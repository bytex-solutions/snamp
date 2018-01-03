package com.bytex.snamp.connector;

import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.JMException;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a base class for management connector bundle.
 * <p>
 *     This bundle activator represents a factory of {@link ManagedResourceConnector} implementations.
 *     Each connector should be registered as separated service in OSGi environment.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public abstract class ManagedResourceActivator extends AbstractServiceLibrary {
    private static final ActivationProperty<String> CONNECTOR_TYPE_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<CMManagedResourceParser> MANAGED_RESOURCE_PARSER_HOLDER = defineActivationProperty(CMManagedResourceParser.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class, Logger.getAnonymousLogger());

    protected static abstract class ManagedResourceLifecycleManager<TConnector extends ManagedResourceConnector> extends ServiceSubRegistryManager<ManagedResourceConnector, TConnector> {
        private final LazyReference<Logger> logger = LazyReference.strong();
        private final Map<String, ManagedResourceConfiguration> resources = new HashMap<>();
        private final ThreadLocal<String> resourceName = new ThreadLocal<>();

        protected ManagedResourceLifecycleManager(final RequiredService<?>... dependencies) {
            super(ManagedResourceConnector.class, dependencies);
        }

        protected ManagedResourceLifecycleManager(final Iterable<Class<? super TConnector>> subcontracts, final RequiredService<?>... dependencies) {
            super(ManagedResourceConnector.class, subcontracts, dependencies);
        }

        private Logger getLoggerImpl(){
            return getActivationPropertyValue(LOGGER_HOLDER);
        }

        @Override
        protected final Logger getLogger() {
            return logger.get(this, ManagedResourceLifecycleManager::getLoggerImpl);
        }

        private String getConnectorType(){
            return getActivationPropertyValue(CONNECTOR_TYPE_HOLDER);
        }

        private CMManagedResourceParser getParser(){
            return getActivationPropertyValue(MANAGED_RESOURCE_PARSER_HOLDER);
        }

        @Override
        protected final String getFactoryPID() {
            return getParser().getFactoryPersistentID(getConnectorType());
        }

        private SingletonMap<String, ? extends ManagedResourceConfiguration> parseConfig(final Dictionary<String, ?> configuration) throws IOException{
            final SingletonMap<String, ? extends ManagedResourceConfiguration> newConfig = getParser().parse(configuration);
            newConfig.getValue().setType(getConnectorType());
            newConfig.getValue().expandParameters();
            return newConfig;
        }

        protected TConnector updateConnector(@Nonnull TConnector connector,
                                                   final ManagedResourceConfiguration configuration) throws Exception {
            final Optional<ManagedResourceConnector.Updater> updater = connector.queryObject(ManagedResourceConnector.Updater.class);
            if (updater.isPresent()) {
                updater.get().update(configuration.getConnectionString(), configuration);
            } else {
                final String resourceName = this.resourceName.get();
                assert !isNullOrEmpty(resourceName);
                connector = createConnector(resourceName, configuration);
            }
            return connector;
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
        protected final TConnector updateService(TConnector connector,
                                           final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends ManagedResourceConfiguration> newConfig = parseConfig(configuration);
            if (!Objects.equals(resources.get(newConfig.getKey()), newConfig.getValue())) {
                resourceName.set(newConfig.getKey());
                try {
                    connector = updateConnector(connector, newConfig.getValue());
                } catch (final Exception e) {
                    resources.remove(newConfig.getKey());
                    throw e;
                } finally {
                    resourceName.remove();
                }
                resources.put(newConfig.getKey(), newConfig.getValue());
                getLogger().info(String.format("Connector %s for resource %s is updated", getConnectorType(), newConfig.getKey()));
            }
            return connector;
        }

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary)} failed.
         * @param logger
         * @param servicePID    The persistent identifier associated with the service.
         * @param configuration The configuration of the service.
         * @param e             An exception occurred when updating service.
         */
        @Override
        protected final void failedToUpdateService(final Logger logger,
                                             final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to update connector '%s'", servicePID), e);
        }

        /**
         * Logs error details when {@link #disposeService(Object, boolean)} failed.
         * @param logger
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e          An exception occurred when disposing service.
         */
        @Override
        protected final void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to dispose connector '%s'", servicePID), e);
        }

        @Nonnull
        protected abstract TConnector createConnector(final String resourceName,
                                             final ManagedResourceConfiguration configuration) throws Exception;

        /**
         * Creates a new service.
         *
         * @param identity      The registration properties to fill.
         * @param configuration A new configuration of the service.
         * @return A new instance of the service.
         * @throws Exception                                  Unable to instantiate a new service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid configuration exception.
         */
        @Override
        protected final TConnector activateService(final ServiceIdentityBuilder identity,
                                             final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends ManagedResourceConfiguration> newConfig = parseConfig(configuration);
            identity.acceptAll(new ManagedResourceSelector(newConfig.getValue()).setResourceName(newConfig.getKey()));
            final TConnector connector = createConnector(newConfig.getKey(), newConfig.getValue());
            resources.put(newConfig.getKey(), newConfig.getValue());
            getLogger().info(String.format("Connector %s for resource %s is instantiated", getConnectorType(), newConfig.getKey()));
            return connector;
        }

        @Override
        protected final void disposeService(final TConnector service,
                                      final Map<String, ?> identity) throws Exception {
            final String resourceName = ManagedResourceSelector.getManagedResourceName(identity);
            resources.remove(resourceName);
            service.close();
            getLogger().info(String.format("Connector %s is destroyed", resourceName));
        }

        @Nonnull
        ProvidedServices serviceProvider(final ProvidedService<?>... optionalServices) {
            return (services, activationProperties, bundleLevelDependencies) -> {
                services.add(this);
                Collections.addAll(services, optionalServices);
            };
        }
    }

    protected static abstract class DefaultManagedResourceLifecycleManager<TConnector extends ManagedResourceConnector> extends ManagedResourceLifecycleManager<TConnector> implements ManagedResourceConnectorFactoryService {
        @FunctionalInterface
        private interface FeatureAdder<D extends FeatureDescriptor<?>> {
            void addFeature(final String featureName, final D descriptor) throws JMException;
        }

        protected DefaultManagedResourceLifecycleManager(final RequiredService<?>... dependencies) {
            super(dependencies);
        }

        protected DefaultManagedResourceLifecycleManager(final Iterable<Class<? super TConnector>> subtracts,
                                                         final RequiredService<?>... dependencies) {
            super(subtracts, dependencies);
        }

        @Nonnull
        public abstract TConnector createConnector(final String resourceName,
                                                   final String connectionString,
                                                   final Map<String, String> configuration) throws Exception;

        private <F extends FeatureConfiguration, D extends FeatureDescriptor<F>> Set<String> configureFeatures(final FeatureAdder<D> adder,
                                                                                                               final Map<String, D> features) {
            final Set<String> registeredFeatures = new HashSet<>();
            for (final Map.Entry<String, D> metadata : features.entrySet()) {
                boolean success;
                try {
                    adder.addFeature(metadata.getKey(), metadata.getValue());
                    success = true;
                } catch (final JMException e) {
                    getLogger().log(Level.SEVERE, "Unable to register feature " + metadata.getKey(), e);
                    success = false;
                }
                if (success)
                    registeredFeatures.add(metadata.getKey());
            }
            return registeredFeatures;
        }

        private <F extends FeatureConfiguration, D extends FeatureDescriptor<F>> Set<String> configureFeatures(final FeatureAdder<D> adder,
                                                                                                               final Function<? super F, ? extends D> descriptorFactory,
                                                                                                               final Map<String, ? extends F> features) {
            return configureFeatures(adder, features.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> descriptorFactory.apply(entry.getValue()))));
        }

        protected void configureAttributes(final TConnector connector,
                                           final Map<String, ? extends AttributeConfiguration> attributes) {
            connector.queryObject(AttributeManager.class).ifPresent(manager -> {
                final Set<String> addedAttributes = configureFeatures(manager::addAttribute, AttributeDescriptor::new, attributes);
                manager.retainAttributes(addedAttributes);
            });
        }

        protected void configureEvents(final TConnector connector,
                                       final Map<String, ? extends EventConfiguration> events) {
            connector.queryObject(NotificationManager.class).ifPresent(manager -> {
                final Set<String> addedEvents = configureFeatures(manager::enableNotifications, NotificationDescriptor::new, events);
                manager.retainNotifications(addedEvents);
            });
        }

        protected void configureOperations(final TConnector connector,
                                           final Map<String, ? extends OperationConfiguration> operations) {
            connector.queryObject(OperationManager.class).ifPresent(manager -> {
                final Set<String> addedEvents = configureFeatures(manager::enableOperation, OperationDescriptor::new, operations);
                manager.retainOperations(addedEvents);
            });
        }

        protected void enableSmartMode(final TConnector connector) {
            connector.queryObject(AttributeManager.class).ifPresent(manager -> configureFeatures(manager::addAttribute, manager.discoverAttributes()));
            connector.queryObject(OperationManager.class).ifPresent(manager -> configureFeatures(manager::enableOperation, manager.discoverOperations()));
            connector.queryObject(NotificationManager.class).ifPresent(manager -> configureFeatures(manager::enableNotifications, manager.discoverNotifications()));
        }

        @Nonnull
        @Override
        protected final TConnector createConnector(final String resourceName, final ManagedResourceConfiguration configuration) throws Exception {
            final TConnector connector = createConnector(resourceName, configuration.getConnectionString(), configuration);
            configureAttributes(connector, configuration.getAttributes());
            configureEvents(connector, configuration.getEvents());
            configureOperations(connector, configuration.getOperations());
            //expansion should be the last instruction in this method because updating procedure
            //may remove all automatically added attributes
            if (configuration.isSmartMode())
                enableSmartMode(connector);
            return connector;
        }

        @Nonnull
        @Override
        final ProvidedServices serviceProvider(final ProvidedService<?>... optionalServices) {
            final class ManagedResourceConnectorFactoryServiceManager extends SupportServiceManager<DefaultManagedResourceLifecycleManager<TConnector>> {
                private ManagedResourceConnectorFactoryServiceManager() {
                    super(ManagedResourceConnectorFactoryService.class);
                }

                @Nonnull
                @Override
                protected DefaultManagedResourceLifecycleManager<TConnector> createService(final ServiceIdentityBuilder identity) {
                    return DefaultManagedResourceLifecycleManager.this;
                }
            }

            return (services, activationProperties, bundleLevelDependencies) -> {
                services.add(this);
                services.add(new ManagedResourceConnectorFactoryServiceManager());
                Collections.addAll(services, optionalServices);
            };
        }
    }

    /**
     * Represents superclass for all-optional resource connector service providers.
     * You cannot derive from this class directly.
     * @param <T> Type of the gateway-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     * @see #configurationDescriptor(Function, RequiredService[])
     */
    protected static abstract class SupportServiceManager<T extends SupportService> extends ProvidedService<T> {

        private SupportServiceManager(final Class<? super T> contract,
                                      final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        protected SupportServiceManager(@Nonnull final Class<? super T> mainContract,
                                        @Nonnull final Class<? super T> secondaryContract,
                                        final RequiredService<?>... dependencies) {
            super(mainContract, secondaryContract, dependencies);
        }

        protected SupportServiceManager(@Nonnull final Class<? super T> mainContract,
                                        @Nonnull final Class<? super T> secondContract,
                                        @Nonnull final Class<? super T> thirdContract,
                                        final RequiredService<?>... dependencies) {
            super(mainContract, secondContract, thirdContract, dependencies);
        }

        @Nonnull
        protected abstract T createService(final ServiceIdentityBuilder identity) throws Exception;

        @Override
        @Nonnull
        protected final T activateService(final ServiceIdentityBuilder identity) throws Exception {
            identity.acceptAll(new ManagedResourceSelector().setConnectorType(getConnectorType()));
            return createService(identity);
        }

        final Logger getLogger(){
            return LoggerProvider.getLoggerForObject(this);
        }

        /**
         * Gets name of the underlying resource connector.
         * @return The name of the underlying resource connector.
         * @see #getState()
         */
        protected final String getConnectorType() {
            return getActivationPropertyValue(CONNECTOR_TYPE_HOLDER);
        }

        static <T extends SupportService> SupportServiceManager<T> create(final Class<? super T> contract,
                                                                                                  final Function<DependencyManager, T> activator,
                                                                                                  final RequiredService<?>... dependencies){
            return new SupportServiceManager<T>(contract, dependencies) {
                @Nonnull
                @Override
                protected T createService(final ServiceIdentityBuilder identity) throws Exception {
                    return activator.apply(super.dependencies);
                }
            };
        }
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<T> configurationDescriptor(final Function<DependencyManager, T> factory, final RequiredService<?>... dependencies) {
        return SupportServiceManager.create(ConfigurationEntityDescriptionProvider.class, factory, dependencies);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<T> configurationDescriptor(final Supplier<T> factory) {
        return configurationDescriptor(dependencies -> factory.get());
    }

    protected final String connectorType;
    private final Logger logger;

    /**
     * Initializes a new connector factory.
     * @param factory Resource connector factory. Cannot be {@literal null}.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceLifecycleManager<?> factory,
                                       final SupportServiceManager<?>... optionalServices) {
        super(factory.serviceProvider(optionalServices));
        connectorType = ManagedResourceConnector.getConnectorType(getBundleContextOfObject(this).getBundle());
        logger = LoggerProvider.getLoggerForObject(this);
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(ConfigurationManager.class, context);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final DependencyManager dependencies) throws Exception {
        activationProperties.publish(CONNECTOR_TYPE_HOLDER, connectorType);
        activationProperties.publish(LOGGER_HOLDER, logger);
        {
            final CMManagedResourceParser parser = dependencies.getService(ConfigurationManager.class)
                    .queryObject(CMManagedResourceParser.class)
                    .orElseThrow(AssertionError::new);
            activationProperties.publish(MANAGED_RESOURCE_PARSER_HOLDER, parser);
        }
        logger.info(String.format("Activating resource connector of type %s", connectorType));
        super.activate(context, activationProperties, dependencies);
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, ActivationPropertyPublisher, DependencyManager)}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected final void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        logger.log(Level.SEVERE, String.format("Unable to instantiate %s connector", connectorType), e);
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
        logger.log(Level.SEVERE, String.format("Unable to release %s connector",
                connectorType), e);
    }

    /**
     * Deactivates this library.
     * <p>
     * This method will be invoked when at least one dependency was lost.
     * </p>
     *
     * @param context              The execution context of the library being deactivated.
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Deactivation error.
     */
    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        super.deactivate(context, activationProperties);
        logger.info(String.format("Unloading connector of type %s", connectorType));
    }

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return connectorType;
    }

    private static List<Bundle> getResourceConnectorBundles(final BundleContext context) {
        return Arrays.stream(context.getBundles())
                .filter(ManagedResourceConnector::isResourceConnectorBundle)
                .collect(Collectors.toList());
    }

    static List<Bundle> getResourceConnectorBundles(final BundleContext context, final String connectorName){
        return Arrays.stream(context.getBundles())
                .filter(bnd -> ManagedResourceConnector.getConnectorType(bnd).equals(connectorName))
                .collect(Collectors.toList());
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
                .map(ManagedResourceConnector::getConnectorType)
                .collect(Collectors.toList());
    }
}