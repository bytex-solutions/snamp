package com.bytex.snamp.connector;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.configuration.internal.CMManagedResourceParser;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.discovery.DiscoveryService;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SupportService;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ObjectArrays;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

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
import static com.bytex.snamp.MapUtils.getValue;
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
    private static final String CATEGORY = "resourceConnector";

    private static final String NAME_PROPERTY = "resourceName";
    private static final String CONNECTION_STRING_PROPERTY = "connectionString";

    private static final ActivationProperty<String> CONNECTOR_TYPE_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents an interface responsible for lifecycle control over resource connector instances.
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
        TConnector createConnector(final String resourceName,
                                   final ManagedResourceInfo configuration,
                                   final DependencyManager dependencies) throws Exception;
    }

    private static final class ManagedResourceConnectorRegistry<TConnector extends ManagedResourceConnector> extends ServiceSubRegistryManager<ManagedResourceConnector, TConnector> {
        private final ManagedResourceConnectorFactory<TConnector> controller;

        /**
         * Represents name of the managed resource connector.
         */
        protected final String connectorType;

        private ManagedResourceConnectorRegistry(final String connectorType,
                                                 final ManagedResourceConnectorFactory<TConnector> controller,
                                                 final RequiredService<?>... dependencies) {
            super(ManagedResourceConnector.class, ObjectArrays.<RequiredService>concat(dependencies, new SimpleDependency<>(ConfigurationManager.class)));
            this.controller = Objects.requireNonNull(controller, "controller is null.");
            this.connectorType = connectorType;
        }

        private ManagedResourceConnectorRegistry(final ManagedResourceConnectorFactory<TConnector> controller,
                                                 final RequiredService<?>... dependencies){
            this(ManagedResourceConnector.getConnectorType(Utils.getBundleContextOfObject(controller).getBundle()), controller, dependencies);
        }

        private CMManagedResourceParser getParser(){
            @SuppressWarnings("unchecked")
            final ConfigurationManager configManager = getDependencies().getDependency(ConfigurationManager.class);
            assert configManager != null;
            final CMManagedResourceParser parser = configManager.queryObject(CMManagedResourceParser.class);
            assert parser != null;
            return parser;
        }

        @Override
        protected String getFactoryPID(final RequiredService<?>[] dependencies) {
            return getParser().getFactoryPersistentID(connectorType);
        }

        private static void setFeatureNameIfNecessary(final FeatureConfiguration feature,
                                                         final String name) {
            if (!feature.containsKey(FeatureConfiguration.NAME_KEY))
                feature.put(FeatureConfiguration.NAME_KEY, name);
        }

        private static <I extends FeatureConfiguration, O extends MBeanFeatureInfo> void updateFeatures(final BiFunction<String, I, O> featureAdder,
                                                                                                        final Function<O, String> nameResolver,
                                                                                                        final Consumer<Set<String>> retainer,
                                                                                                        final Map<String, ? extends I> features) {
            final Set<String> addedFeatures = features.entrySet().stream()
                    .map(entry -> {
                        setFeatureNameIfNecessary(entry.getValue(), entry.getKey());
                        return featureAdder.apply(entry.getKey(), entry.getValue());
                    })
                    .filter(Objects::nonNull)
                    .map(nameResolver)
                    .collect(Collectors.toSet());
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
                    metadata -> ArrayUtils.getFirst(metadata.getNotifTypes()),
                    connector::retainNotifications,
                    events
            );
        }

        private void updateFeatures(final TConnector connector,
                            final ManagedResourceConfiguration configuration) throws Exception {
            Aggregator.queryAndAccept(connector,
                    AttributeSupport.class,
                    attributeSupport -> updateAttributes(attributeSupport, configuration.getFeatures(AttributeConfiguration.class)));
            Aggregator.queryAndAccept(connector,
                    NotificationSupport.class,
                    notificationSupport -> updateEvents(notificationSupport, configuration.getFeatures(EventConfiguration.class)));
            Aggregator.queryAndAccept(connector,
                    OperationSupport.class,
                    operationSupport -> updateOperations(operationSupport, configuration.getFeatures(OperationConfiguration.class)));
            //expansion should be the last instruction in this method because updating procedure
            //may remove all automatically added attributes
            connector.expandAll();
        }

        private static ManagedResourceConfiguration getNewConfiguration(final String resourceName, final ConfigurationManager manager) throws IOException {
            return manager.transformConfiguration(config -> config.getEntities(ManagedResourceConfiguration.class).get(resourceName));
        }

        private TConnector update(TConnector connector,
                                  final String resourceName,
                                  final ManagedResourceConfiguration newConfig) throws Exception {
            //we should not update resource connector if connection parameters was not changed
            if (!connector.getConfiguration().equals(newConfig)) {
                //trying to update resource connector on-the-fly
                try {
                    connector.update(newConfig);
                } catch (final ManagedResourceConnector.UnsupportedUpdateOperationException ignored) {
                    //Update operation is not supported -> force recreation
                    connector.close();
                    connector = controller.createConnector(resourceName, newConfig, getDependencies());
                }
            }
            //but we should always update resource features
            updateFeatures(connector, newConfig);
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
        protected TConnector update(final TConnector connector,
                                    final Dictionary<String, ?> configuration) throws Exception {
            final CMManagedResourceParser parser = getParser();
            final String resourceName = parser.getResourceName(configuration);
            @SuppressWarnings("unchecked")
            final ManagedResourceConfiguration newConfig = getNewConfiguration(resourceName, getDependencies().getDependency(ConfigurationManager.class));
            if(newConfig == null)
                throw new IllegalStateException(String.format("Managed resource %s cannot be updated. Configuration not found.", resourceName));
            return update(connector, resourceName, newConfig);
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

        private TConnector createService(final Map<String, Object> identity,
                                         final String resourceName,
                                         final ManagedResourceConfiguration configuration) throws Exception {
            identity.putAll(configuration);
            identity.put(NAME_PROPERTY, resourceName);
            identity.put(ManagedResourceConnector.TYPE_CAPABILITY_ATTRIBUTE, connectorType);
            identity.put(ManagedResourceConnector.CATEGORY_PROPERTY, CATEGORY);
            identity.put(CONNECTION_STRING_PROPERTY, configuration.getConnectionString());
            final TConnector result = controller.createConnector(resourceName, configuration, getDependencies());
            updateFeatures(result, configuration);
            return result;
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
            final CMManagedResourceParser parser = getParser();
            final String resourceName = parser.getResourceName(configuration);
            @SuppressWarnings("unchecked")
            final ManagedResourceConfiguration newConfig = getNewConfiguration(resourceName, getDependencies().getDependency(ConfigurationManager.class));
            if(newConfig == null)
                throw new IllegalStateException(String.format("Managed resource %s cannot be created. Configuration not found.", resourceName));
            return createService(identity, resourceName, newConfig);
        }

        @Override
        protected void cleanupService(final TConnector service,
                                      final Map<String, ?> identity) throws Exception {
            service.close();
        }
    }

    private static class ManagedResourceConnectorFactoryServiceImpl<TConnector extends ManagedResourceConnector> extends AbstractAggregator implements ManagedResourceConnectorFactoryService {
        private final ManagedResourceConnectorFactory<TConnector> connectorFactory;
        private final DependencyManager dependencies;

        private ManagedResourceConnectorFactoryServiceImpl(final ManagedResourceConnectorFactory<TConnector> factory,
                                                           final DependencyManager dependencies) {
            this.connectorFactory = Objects.requireNonNull(factory);
            this.dependencies = dependencies;
        }
        
        @Override
        public TConnector createConnector(final String resourceName, final ManagedResourceInfo configuration) throws Exception {
            return connectorFactory.createConnector(resourceName, configuration, dependencies);
        }
    }

    /**
     * Represents activator for support service.
     * @param <T> Type of support service.
     * @since 2.0
     */
    @FunctionalInterface
    protected interface SupportServiceActivator<T extends SupportService>{
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
     * @see #discoveryService(SupportServiceActivator, RequiredService[])
     * @see #configurationDescriptor(SupportServiceActivator, RequiredService[])
     */
    protected static abstract class SupportConnectorServiceManager<S extends SupportService, T extends S> extends ProvidedService<S, T> {

        private SupportConnectorServiceManager(final Class<S> contract,
                                               final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        abstract T activateService() throws Exception;

        @Override
        protected final T activateService(final Map<String, Object> identity) throws Exception {
            identity.put(ManagedResourceConnector.TYPE_CAPABILITY_ATTRIBUTE, getConnectorType());
            identity.put(ManagedResourceConnector.CATEGORY_PROPERTY, CATEGORY);
            return activateService();
        }

        final Logger getLogger(){
            return getActivationPropertyValue(LOGGER_HOLDER);
        }

        /**
         * Gets name of the underlying resource connector.
         * @return The name of the underlying resource connector.
         * @see #getState()
         */
        private String getConnectorType() {
            return getActivationPropertyValue(CONNECTOR_TYPE_HOLDER);
        }

        private static <S extends SupportService, T extends S> SupportConnectorServiceManager<S, T> create(final Class<S> contract,
                                                                                                           final SupportServiceActivator<T> activator,
                                                                                                           final RequiredService<?>... dependencies){
            return new SupportConnectorServiceManager<S, T>(contract, dependencies) {
                @Override
                T activateService() throws Exception {
                    return activator.activateService(getDependencies());
                }
            };
        }
    }

    /**
     * Initializes a new connector factory.
     * @param controller Resource connector lifecycle controller. Cannot be {@literal null}.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceConnectorFactory<TConnector> controller,
                                       final SupportConnectorServiceManager<?, ?>... optionalServices) {
        this(controller,
                emptyArray(RequiredService[].class),
                optionalServices);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportConnectorServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final SupportServiceActivator<T> factory,
                                                                                                                                                                          final RequiredService<?>... dependencies) {
        return SupportConnectorServiceManager.create(ConfigurationEntityDescriptionProvider.class, factory, dependencies);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportConnectorServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final Supplier<T> factory){
        return configurationDescriptor(dependencies -> factory.get());
    }

    protected static <T extends DiscoveryService> SupportConnectorServiceManager<DiscoveryService, T> discoveryService(final SupportServiceActivator<T> factory,
                                                                                                                       final RequiredService<?>... dependencies) {
        return SupportConnectorServiceManager.create(DiscoveryService.class, factory, dependencies);
    }

    private static <TConnector extends ManagedResourceConnector> SupportConnectorServiceManager<ManagedResourceConnectorFactoryService, ?> factoryService(final ManagedResourceConnectorFactory<TConnector> factory,
                                                                                                                                                          final RequiredService<?>... dependencies){
        return new SupportConnectorServiceManager<ManagedResourceConnectorFactoryService, ManagedResourceConnectorFactoryServiceImpl<TConnector>>(ManagedResourceConnectorFactoryService.class, dependencies) {
            @Override
            ManagedResourceConnectorFactoryServiceImpl<TConnector> activateService() {
                return new ManagedResourceConnectorFactoryServiceImpl<>(factory, getDependencies());
            }
        };
    }

    /**
     * Initializes a new connector factory.
     * @param controller Resource connector lifecycle controller. Cannot be {@literal null}.
     * @param connectorDependencies A collection of connector-level dependencies.
     * @param optionalServices Additional set of supporting services.
     */
    protected ManagedResourceActivator(final ManagedResourceConnectorFactory<TConnector> controller,
                                       final RequiredService<?>[] connectorDependencies,
                                       final SupportConnectorServiceManager<?, ?>[] optionalServices){
        super(ObjectArrays.concat(optionalServices, new ProvidedService<?, ?>[]{ new ManagedResourceConnectorRegistry<>(controller, connectorDependencies), factoryService(controller, connectorDependencies)}, ProvidedService.class));
    }

    /**
     * Gets type of the connector.
     * @return Type of the connector.
     */
    public final String getConnectorType(){
        return ManagedResourceConnector.getConnectorType(Utils.getBundleContextOfObject(this).getBundle());
    }

    /**
     * Adds global dependencies.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param dependencies A collection of connector's global dependencies.
     */
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
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {
        activationProperties.publish(LOGGER_HOLDER, getLogger());
        activationProperties.publish(CONNECTOR_TYPE_HOLDER, getConnectorType());
        getLogger().info(String.format("Activating resource connector of type %s", getConnectorType()));
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.bytex.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher)}  method.
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
        getLogger().log(Level.SEVERE, String.format("Unable to release %s connector",
                getConnectorType()), e);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        getLogger().info(String.format("Unloading connector of type %s", getConnectorType()));
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

    private static String getReferencePropertyAsString(final ServiceReference<ManagedResourceConnector> connectorRef,
                                                       final String propertyName,
                                                       final String defaultValue){
        return connectorRef != null ?
                Objects.toString(connectorRef.getProperty(propertyName), defaultValue) :
                defaultValue;
    }

    static String getManagedResourceName(final ServiceReference<ManagedResourceConnector> connectorRef) {
        return getReferencePropertyAsString(connectorRef, NAME_PROPERTY, "");
    }

    static String getConnectionString(final ServiceReference<ManagedResourceConnector> identity){
        return getValue(getProperties(identity), CONNECTION_STRING_PROPERTY, Objects::toString).orElse("");
    }

    private static String getManagedResourceName(final Map<String, ?> identity){
        return getValue(identity, NAME_PROPERTY, String.class).orElse("");
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

    static String createFilter(final String connectorType, final String filter) {
        return isNullOrEmpty(filter) ?
                String.format("(&(%s=%s)(%s=%s))", ManagedResourceConnector.CATEGORY_PROPERTY, CATEGORY, ManagedResourceConnector.TYPE_CAPABILITY_ATTRIBUTE, connectorType) :
                String.format("(&(%s=%s)(%s=%s)%s)", ManagedResourceConnector.CATEGORY_PROPERTY, CATEGORY, ManagedResourceConnector.TYPE_CAPABILITY_ATTRIBUTE, connectorType, filter);
    }

    static String createFilter(final String resourceName) {
        return String.format("(&(%s=%s)(%s=%s))", ManagedResourceConnector.CATEGORY_PROPERTY, CATEGORY, NAME_PROPERTY, resourceName);
    }
}