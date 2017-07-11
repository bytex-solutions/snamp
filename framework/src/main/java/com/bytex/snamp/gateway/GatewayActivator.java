package com.bytex.snamp.gateway;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents lifetime manager for gateway instances.
 * <p>
 *     This class is a recommendation for the gateway implementation. Of course,
 *     you can write your own {@link org.osgi.framework.BundleActivator} implementation
 *     and consumes instances of {@link com.bytex.snamp.connector.ManagedResourceConnector} services.
 * </p>
 * @param <G> Type of the management gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class GatewayActivator<G extends Gateway> extends AbstractServiceLibrary {
    private static final ActivationProperty<String> GATEWAY_TYPE_HOLDER = defineActivationProperty(String.class, "");
    private static final ActivationProperty<CMGatewayParser> GATEWAY_PARSER_HOLDER = defineActivationProperty(CMGatewayParser.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class, Logger.getAnonymousLogger());

    /**
     * Represents a factory responsible for creating instances of a gateway.
     * @param <G> Type of gateway implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    @FunctionalInterface
    protected interface GatewayFactory<G extends Gateway>{
        @Nonnull
        G createInstance(final String gatewayInstance,
                         final DependencyManager dependencies) throws Exception;

        default Class<? super G>[] extraContracts(){
            return ArrayUtils.toArray(Gateway.class);
        }
    }

    private static final class GatewayInstances<G extends Gateway> extends ServiceSubRegistryManager<Gateway, G>{
        private final GatewayFactory<G> gatewayInstanceFactory;
        private final LazyReference<Logger> logger;

        private GatewayInstances(@Nonnull final GatewayFactory<G> factory,
                                 final RequiredService<?>... dependencies) {
            super(Gateway.class, dependencies);
            this.gatewayInstanceFactory = factory;
            this.logger = LazyReference.strong();
        }

        private Logger getLoggerImpl(){
            return getActivationPropertyValue(LOGGER_HOLDER);
        }

        @Override
        protected Logger getLogger() {
            return logger.lazyGet(this, GatewayInstances::getLoggerImpl);
        }

        private String getGatewayType(){
            return getActivationPropertyValue(GATEWAY_TYPE_HOLDER);
        }

        private CMGatewayParser getParser(){
            return getActivationPropertyValue(GATEWAY_PARSER_HOLDER);
        }

        @Override
        protected String getFactoryPID() {
            return getParser().getFactoryPersistentID(getGatewayType());
        }

        private SingletonMap<String, ? extends GatewayConfiguration> parseConfig(final Dictionary<String, ?> configuration) throws IOException{
            final SingletonMap<String, ? extends GatewayConfiguration> newConfig = getParser().parse(configuration);
            newConfig.getValue().setType(getGatewayType());
            newConfig.getValue().expandParameters();
            return newConfig;
        }

        @Override
        protected G updateService(final G gatewayInstance,
                                  final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends GatewayConfiguration> newConfig = parseConfig(configuration);
            gatewayInstance.update(newConfig.getValue());
            getLogger().info(String.format("Gateway %s is updated", gatewayInstance));
            return gatewayInstance;
        }

        @Override
        protected G activateService(final BiConsumer<String, Object> identity,
                                    final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends GatewayConfiguration> newConfig = parseConfig(configuration);
            new GatewaySelector(newConfig.getValue()).setInstanceName(newConfig.getKey()).forEach(identity);
            final G gatewayInstance = gatewayInstanceFactory.createInstance(newConfig.getKey(), dependencies);
            gatewayInstance.update(newConfig.getValue());
            getLogger().info(String.format("Gateway %s is instantiated", gatewayInstance));
            return gatewayInstance;
        }

        @Override
        protected void disposeService(final G gatewayInstance, final Map<String, ?> identity) throws IOException {
            getLogger().info(String.format("Gateway %s is destroyed", gatewayInstance));
            gatewayInstance.close();
        }

        @Override
        protected void failedToUpdateService(final Logger logger,
                                             final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e) {
            logger.log(Level.SEVERE,
                    String.format("Unable to update gateway. Type: %s, instance: %s",
                            getGatewayType(),
                            servicePID),
                    e);
        }

        @Override
        protected void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to release gateway. Type: %s, instance: %s", getGatewayType(), servicePID),
                    e);
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
     * Represents superclass for all optional gateway-related service factories.
     * You cannot derive from this class directly.
     * @param <S> Type of the gateway-related service contract.
     * @param <T> Type of the gateway-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     * @see #configurationDescriptor(Supplier)
     */
    protected final static class SupportServiceManager<S extends SupportService, T extends S> extends ProvidedService<S, T>{
        private final SupportServiceFactory<T> activator;

        private SupportServiceManager(final Class<S> contract,
                                      final SupportServiceFactory<T> activator,
                                      final RequiredService<?>... dependencies) {
            super(contract, dependencies);
            this.activator = Objects.requireNonNull(activator);
        }

        @Override
        @Nonnull
        protected T activateService(final Map<String, Object> identity) throws Exception {
            identity.putAll(new GatewaySelector().setGatewayType(getGatewayType()));
            return activator.activateService(dependencies);
        }

        /**
         * Gets type of the gateway.
         * @return Type of the gateway.
         */
        private String getGatewayType(){
            return getActivationPropertyValue(GATEWAY_TYPE_HOLDER);
        }
    }

    protected final String gatewayType;
    final Logger logger;

    /**
     * Initializes a new instance of the gateway activator.
     * @param factory Gateway factory. Cannot be {@literal null}.
     * @param optionalServices Additional services exposed by gateway.
     */
    protected GatewayActivator(final GatewayFactory<G> factory,
                               final SupportServiceManager<?, ?>... optionalServices){
        this(factory, emptyArray(RequiredService[].class), optionalServices);
    }

    /**
     * Initializes a new instance of the gateway activator.
     * @param factory Gateway factory. Cannot be {@literal null}.
     * @param gatewayDependencies Gateway-level dependencies.
     * @param optionalServices Additional services exposed by gateway.
     */
    protected GatewayActivator(final GatewayFactory<G> factory,
                               final RequiredService<?>[] gatewayDependencies,
                               final SupportServiceManager<?, ?>[] optionalServices) {
        super(serviceProvider(factory, gatewayDependencies, optionalServices));
        gatewayType = Gateway.getGatewayType(getBundleContextOfObject(this).getBundle());
        logger = LoggerProvider.getLoggerForObject(this);
    }

    private static <G extends Gateway> ProvidedServices serviceProvider(final GatewayFactory<G> factory,
                                                                        final RequiredService<?>[] gatewayDependencies,
                                                    final SupportServiceManager<?, ?>[] optionalServices){
        return (services, activationProperties, bundleLevelDependencies) -> {
            services.add(new GatewayInstances<>(factory, gatewayDependencies));
            Collections.addAll(services, optionalServices);
        };
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final SupportServiceFactory<T> factory,
                                                                                                                                                                 final RequiredService<?>... dependencies) {
        return new SupportServiceManager<>(ConfigurationEntityDescriptionProvider.class, factory, dependencies);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final Supplier<T> factory) {
        return configurationDescriptor(dependencies -> factory.get());
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
        activationProperties.publish(GATEWAY_TYPE_HOLDER, gatewayType);
        activationProperties.publish(LOGGER_HOLDER, logger);
        {
            final CMGatewayParser parser = dependencies.getService(ConfigurationManager.class)
                    .flatMap(manager -> manager.queryObject(CMGatewayParser.class))
                    .orElseThrow(AssertionError::new);
            activationProperties.publish(GATEWAY_PARSER_HOLDER, parser);
        }
        logger.info(String.format("Activating gateway of type %s", gatewayType));
        super.activate(context, activationProperties, dependencies);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        super.deactivate(context, activationProperties);
        logger.info(String.format("Unloading gateway of type %s", gatewayType));
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, ActivationPropertyPublisher, DependencyManager)}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        logger.log(Level.SEVERE, String.format("Unable to activate %s gateway instance",
                gatewayType),
                e);
    }

    /**
     * Handles an exception thrown by {@link } method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        logger.log(Level.SEVERE, String.format("Unable to deactivate %s gateway instance",
                        gatewayType),
                e);
    }

    private static List<Bundle> getGatewayBundles(final BundleContext context){
        return Arrays.stream(context.getBundles())
                .filter(Gateway::isGatewayBundle)
                .collect(Collectors.toList());
    }

    static List<Bundle> getGatewayBundles(final BundleContext context, final String gatewayType) {
        return Arrays.stream(context.getBundles())
                .filter(bnd -> Gateway.getGatewayType(bnd).equals(gatewayType))
                .collect(Collectors.toList());
    }

    /**
     * Disables all gateways loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @return Number of stopped bundles.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop gateway.
     */
    public static int disableGateways(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalStateException("context is null.");
        int count = 0;
        for(final Bundle bnd: getGatewayBundles(context)) {
            bnd.stop();
            count += 1;
        }
        return count;
    }

    /**
     * Disables gateway by its type.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param gatewayType The type of gateway to disable.
     * @return {@literal true}, if bundle with the specified gateway exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop gateway.
     */
    public static boolean disableGateway(final BundleContext context, final String gatewayType) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getGatewayBundles(context, gatewayType)) {
            bnd.stop();
            success = true;
        }
        return success;
    }

    /**
     * Enables all gateways loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @return Number of started bundles with gateway.
     * @throws BundleException Unable to start gateway.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static int enableGateways(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        int count = 0;
        for(final Bundle bnd: getGatewayBundles(context)) {
            bnd.start();
            count += 1;
        }
        return count;
    }

    /**
     * Enables the specified gateway.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param gatewayType The type of gateway to enable.
     * @return {@literal true}, if bundle with the specified gateway exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start bundle with gateway.
     */
    public static boolean enableGateway(final BundleContext context, final String gatewayType) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getGatewayBundles(context, gatewayType)) {
            bnd.start();
            success = true;
        }
        return success;
    }


    /**
     * Gets a collection of installed gateways (types).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed gateways.
     */
    public static Collection<String> getInstalledGateways(final BundleContext context) {
        final Collection<Bundle> candidates = getGatewayBundles(context);
        return candidates.stream()
                .map(Gateway::getGatewayType)
                .filter(name -> !isNullOrEmpty(name))
                .collect(Collectors.toSet());
    }
}
