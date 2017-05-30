package com.bytex.snamp.connector;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanOperationInfo;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

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
public abstract class ManagedResourceActivator<TConnector extends ManagedResourceConnector> extends AbstractServiceLibrary {
    private static final ActivationProperty<String> CONNECTOR_TYPE_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<CMManagedResourceParser> MANAGED_RESOURCE_PARSER_HOLDER = defineActivationProperty(CMManagedResourceParser.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class, Logger.getAnonymousLogger());

    /**
     * Represents an interface responsible for instantiating {@link ManagedResourceConnector}.
     * @param <TConnector> Type of the managed resource connector implementation.
     * @author Roman Sakno
     * @since 2.0
     * @version 2.0
     */
    protected interface ManagedResourceConnectorFactory<TConnector extends ManagedResourceConnector> {
        /**
         * Creates a new instance of the managed resource connector.
         * @param resourceName The name of the managed resource.
         * @param configuration Configuration of the managed resource.
         * @param dependencies A collection of connector dependencies.
         * @return A new instance of the resource connector.
         * @throws Exception Unable to instantiate managed resource connector.
         */
        @Nonnull
        TConnector createConnector(final String resourceName,
                                   final ManagedResourceInfo configuration,
                                   final DependencyManager dependencies) throws Exception;
    }

    private static final class ManagedResourceConnectorRegistry<TConnector extends ManagedResourceConnector> extends ServiceSubRegistryManager<ManagedResourceConnector, TConnector> {
        private final ManagedResourceConnectorFactory<TConnector> factory;
        private final LazyStrongReference<Logger> logger;

        private ManagedResourceConnectorRegistry(@Nonnull final ManagedResourceConnectorFactory<TConnector> controller,
                                                 final RequiredService<?>... dependencies) {
            super(ManagedResourceConnector.class, dependencies);
            this.factory = controller;
            this.logger = new LazyStrongReference<>();
        }

        private Logger getLoggerImpl(){
            return getActivationPropertyValue(LOGGER_HOLDER);
        }

        @Override
        protected Logger getLogger() {
            return logger.lazyGet(this, ManagedResourceConnectorRegistry::getLoggerImpl);
        }

        private String getConnectorType(){
            return getActivationPropertyValue(CONNECTOR_TYPE_HOLDER);
        }

        private CMManagedResourceParser getParser(){
            return getActivationPropertyValue(MANAGED_RESOURCE_PARSER_HOLDER);
        }

        @Override
        protected String getFactoryPID() {
            return getParser().getFactoryPersistentID(getConnectorType());
        }

        private static <I extends FeatureConfiguration, O extends MBeanFeatureInfo> void updateFeatures(final BiFunction<String, I, Optional<O>> featureAdder,
                                                                                                        final Function<O, String> nameResolver,
                                                                                                        final Consumer<Set<String>> retainer,
                                                                                                        final Map<String, ? extends I> features) {
            final Set<String> addedFeatures = new HashSet<>(10);
            features.entrySet().stream()
                    .map(entry -> featureAdder.apply(entry.getKey(), entry.getValue()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(nameResolver)
                    .forEach(addedFeatures::add);
            retainer.accept(addedFeatures);
        }

        private static void updateAttributes(final AttributeSupport connector,
                                      final Map<String, ? extends AttributeConfiguration> attributes) {
            updateFeatures(
                    (name, config) -> connector.addAttribute(name, new AttributeDescriptor(config)),
                    MBeanAttributeInfo::getName,
                    connector::retainAttributes,
                    attributes
            );
        }

        private static void updateOperations(final OperationSupport connector,
                                      final Map<String, ? extends OperationConfiguration> operations){
            updateFeatures(
                    (name, config) -> connector.enableOperation(name, new OperationDescriptor(config)),
                    MBeanOperationInfo::getName,
                    connector::retainOperations,
                    operations
            );
        }

        private static void updateEvents(final NotificationSupport connector,
                                  final Map<String, ? extends EventConfiguration> events){
            updateFeatures(
                    (name, config) -> connector.enableNotifications(name, new NotificationDescriptor(config)),
                    metadata -> ArrayUtils.getFirst(metadata.getNotifTypes()).orElseThrow(AssertionError::new),
                    connector::retainNotifications,
                    events
            );
        }

        private static void updateFeatures(final ManagedResourceConnector connector,
                            final ManagedResourceConfiguration configuration) throws Exception {
            connector.queryObject(AttributeSupport.class)
                    .ifPresent(attributeSupport -> updateAttributes(attributeSupport, configuration.getAttributes()));
            connector.queryObject(NotificationSupport.class)
                    .ifPresent(notificationSupport -> updateEvents(notificationSupport, configuration.getEvents()));
            connector.queryObject(OperationSupport.class)
                    .ifPresent(operationSupport -> updateOperations(operationSupport, configuration.getOperations()));
            //expansion should be the last instruction in this method because updating procedure
            //may remove all automatically added attributes
            connector.expandAll();
        }

        private SingletonMap<String, ? extends ManagedResourceConfiguration> parseConfig(final Dictionary<String, ?> configuration) throws IOException{
            final SingletonMap<String, ? extends ManagedResourceConfiguration> newConfig = getParser().parse(configuration);
            newConfig.getValue().setType(getConnectorType());
            newConfig.getValue().expandParameters();
            return newConfig;
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
                                    final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends ManagedResourceConfiguration> newConfig = parseConfig(configuration);
            //we should not update resource connector if connection parameters was not changed
            if (!connector.getConfiguration().equals(newConfig.getValue())) {
                //trying to update resource connector on-the-fly
                try {
                    connector.update(newConfig.getValue());
                } catch (final ManagedResourceConnector.UnsupportedUpdateOperationException ignored) {
                    //Update operation is not supported -> force recreation
                    //previous version of connector will be closed automatically using cleanupService
                    connector = factory.createConnector(newConfig.getKey(), newConfig.getValue(), dependencies);
                }
                updateFeatures(connector, newConfig.getValue());
                getLogger().info(String.format("Connector %s for resource %s is updated", getConnectorType(), newConfig.getKey()));
            }
            else{
                getLogger().info(String.format("Connector %s for resource %s was not changed", getConnectorType(), newConfig.getKey()));
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
         * @return A new instance of the service.
         * @throws Exception                                  Unable to instantiate a new service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid configuration exception.
         */
        @Override
        protected TConnector createService(final Map<String, Object> identity,
                                           final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends ManagedResourceConfiguration> newConfig = parseConfig(configuration);
            identity.putAll(new ManagedResourceFilterBuilder(newConfig.getValue()).setResourceName(newConfig.getKey()));
            final TConnector connector = factory.createConnector(newConfig.getKey(), newConfig.getValue(), dependencies);
            updateFeatures(connector, newConfig.getValue());
            getLogger().info(String.format("Connector %s for resource %s is instantiated", getConnectorType(), newConfig.getKey()));
            return connector;
        }

        @Override
        protected void cleanupService(final TConnector service,
                                      final Map<String, ?> identity) throws Exception {
            getLogger().info(String.format("Connector %s is destroyed", ManagedResourceFilterBuilder.getManagedResourceName(identity)));
            service.close();
        }

        @Override
        protected void destroyed() {
            logger.reset();
        }
    }

    /**
     * Represents activator for support service.
     * @param <T> Type of support service.
     * @since 2.0
     */
    @FunctionalInterface
    protected interface SupportServiceFactory<T extends SupportService>{
        @Nonnull
        T activateService(final DependencyManager dependencies) throws Exception;
    }

    /**
     * Represents superclass for all-optional resource connector service providers.
     * You cannot derive from this class directly.
     * @param <S> Type of the gateway-related service contract.
     * @param <T> Type of the gateway-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     * @see #configurationDescriptor(SupportServiceFactory, RequiredService[])
     */
    protected static abstract class SupportServiceManager<S extends SupportService, T extends S> extends ProvidedService<S, T> {

        private SupportServiceManager(final Class<S> contract,
                                      final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        protected SupportServiceManager(final Class<S> contract,
                                        final RequiredService<?>[] dependencies,
                                        final Class<? super S> contracts){
            super(contract, dependencies, contracts);
        }

        @Nonnull
        protected abstract T createService(final Map<String, Object> identity) throws Exception;

        @Override
        @Nonnull
        protected final T activateService(final Map<String, Object> identity) throws Exception {
            identity.putAll(new ManagedResourceFilterBuilder().setConnectorType(getConnectorType()));
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

        static <S extends SupportService, T extends S> SupportServiceManager<S, T> create(final Class<S> contract,
                                                                                                  final SupportServiceFactory<T> activator,
                                                                                                  final RequiredService<?>... dependencies){
            return new SupportServiceManager<S, T>(contract, dependencies) {
                @Nonnull
                @Override
                protected T createService(final Map<String, Object> identity) throws Exception {
                    return activator.activateService(super.dependencies);
                }
            };
        }
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final SupportServiceFactory<T> factory,
                                                                                                                                                                 final RequiredService<?>... dependencies) {
        return SupportServiceManager.create(ConfigurationEntityDescriptionProvider.class, factory, dependencies);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final Supplier<T> factory){
        return configurationDescriptor(dependencies -> factory.get());
    }

    protected final String connectorType;
    private final Logger logger;

    /**
     * Initializes a new connector factory.
     * @param factory Resource connector factory. Cannot be {@literal null}.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceConnectorFactory<TConnector> factory,
                                       final SupportServiceManager<?, ?>... optionalServices) {
        this(factory,
                emptyArray(RequiredService[].class),
                optionalServices);
    }

    /**
     * Initializes a new connector factory.
     * @param factory Resource connector factory. Cannot be {@literal null}.
     * @param connectorDependencies A collection of connector-level dependencies.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceConnectorFactory<TConnector> factory,
                                       final RequiredService<?>[] connectorDependencies,
                                       final SupportServiceManager<?, ?>[] optionalServices) {
        super(serviceProvider(factory, connectorDependencies, optionalServices));
        connectorType = ManagedResourceConnector.getConnectorType(getBundleContextOfObject(this).getBundle());
        logger = LoggerProvider.getLoggerForObject(this);
    }

    private static <TConnector extends ManagedResourceConnector> ProvidedServices serviceProvider(final ManagedResourceConnectorFactory<TConnector> controller,
                                                                                                  final RequiredService<?>[] connectorDependencies,
                                                                                                  final SupportServiceManager<?, ?>[] optionalServices){
        final class ManagedResourceConnectorFactoryServiceImpl extends AbstractAggregator implements ManagedResourceConnectorFactoryService {
            private final DependencyManager dependencies;

            ManagedResourceConnectorFactoryServiceImpl(final DependencyManager dependencies){
                this.dependencies = Objects.requireNonNull(dependencies);
            }

            @Override
            public TConnector createConnector(final String resourceName, final ManagedResourceInfo configuration) throws Exception {
                return controller.createConnector(resourceName, configuration, dependencies);
            }
        }

        final class ManagedResourceConnectorFactoryServiceManager extends SupportServiceManager<ManagedResourceConnectorFactoryService, ManagedResourceConnectorFactoryServiceImpl> {
            private ManagedResourceConnectorFactoryServiceManager(){
                super(ManagedResourceConnectorFactoryService.class, connectorDependencies);
            }

            @Nonnull
            @Override
            protected ManagedResourceConnectorFactoryServiceImpl createService(final Map<String, Object> identity) {
                return new ManagedResourceConnectorFactoryServiceImpl(dependencies);
            }
        }

        return (services, activationProperties, bundleLevelDependencies) -> {
            services.add(new ManagedResourceConnectorRegistry<>(controller, connectorDependencies));
            services.add(new ManagedResourceConnectorFactoryServiceManager());
            Collections.addAll(services, optionalServices);
        };
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
                    .flatMap(manager -> manager.queryObject(CMManagedResourceParser.class))
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

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP resource connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP resource connector; otherwise, {@literal false}.
     */
    public final boolean equals(final ManagedResourceActivator<?> factory){
        return factory != null && Objects.equals(connectorType, factory.connectorType);
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
        return connectorType.hashCode();
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