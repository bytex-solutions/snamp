package com.bytex.snamp.gateway;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.internal.CMGatewayParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.SupportService;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ObjectArrays;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
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
public class GatewayActivator<G extends AbstractGateway> extends AbstractServiceLibrary {
    private static final String GATEWAY_INSTANCE_IDENTITY_PROPERTY = "instanceName";
    private static final String GATEWAY_TYPE_IDENTITY_PROPERTY = "gatewayType";
    private static final ActivationProperty<String> GATEWAY_TYPE_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents a factory responsible for creating instances of a gateway.
     * @param <G> Type of gateway implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    @FunctionalInterface
    protected interface GatewayFactory<G extends Gateway>{
        G createInstance(final String gatewayInstance,
                         final RequiredService<?>... dependencies) throws Exception;
    }

    private static final class GatewayInstances<G extends AbstractGateway> extends ServiceSubRegistryManager<Gateway, G>{
        private final GatewayFactory<G> gatewayInstanceFactory;
        /**
         * Represents type of gateway.
         */
        protected final String gatewayType;

        private GatewayInstances(final String gatewayType,
                                 final GatewayFactory<G> factory,
                                 final RequiredService<?>... dependencies) {
            super(Gateway.class,
                    ObjectArrays.<RequiredService>concat(dependencies, new SimpleDependency<>(ConfigurationManager.class)));
            this.gatewayInstanceFactory = Objects.requireNonNull(factory, "factory is null.");
            this.gatewayType = gatewayType;
        }

        private GatewayInstances(final GatewayFactory<G> factory,
                                 final RequiredService<?>... dependencies) {
            this(Gateway.getGatewayType(Utils.getBundleContextOfObject(factory).getBundle()), factory, dependencies);
        }

        @SuppressWarnings("unchecked")
        private static CMGatewayParser getParser(final RequiredService<?>... dependencies){
            final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
            assert configManager != null;
            final CMGatewayParser parser = configManager.queryObject(CMGatewayParser.class);
            assert parser != null;
            return parser;
        }

        @Override
        protected String getFactoryPID(final RequiredService<?>[] dependencies) {
            return getParser(dependencies).getFactoryPersistentID(gatewayType);
        }

        private G update(final G gatewayInstance,
                         final GatewayConfiguration configuration) throws Exception {
            gatewayInstance.tryUpdate(configuration.getParameters());
            return gatewayInstance;
        }

        @Override
        protected G update(final G gatewayInstance,
                           final Dictionary<String, ?> configuration,
                           final RequiredService<?>... dependencies) throws Exception {
            final CMGatewayParser parser = getParser(dependencies);
            final String instanceName = parser.getInstanceName(configuration);
            @SuppressWarnings("unchecked")
            final GatewayConfiguration newConfig = getNewConfiguration(instanceName, getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies));
            if (newConfig == null)
                throw new IllegalStateException(String.format("Gateway %s cannot be updated. Configuration not found.", instanceName));
            return update(gatewayInstance, newConfig);
        }

        private static GatewayConfiguration getNewConfiguration(final String instanceName, final ConfigurationManager manager) throws IOException {
            return manager.transformConfiguration(config -> config.getEntities(GatewayConfiguration.class).get(instanceName));
        }

        private G createService(final Map<String, Object> identity,
                                final String instanceName,
                                final GatewayConfiguration configuration,
                                final RequiredService<?>... dependencies) throws Exception{
            createIdentity(gatewayType, instanceName, identity);
            final G gatewayInstance = gatewayInstanceFactory.createInstance(instanceName, dependencies);
            if (gatewayInstance != null)
                if (gatewayInstance.tryStart(configuration.getParameters())) {
                    return gatewayInstance;
                } else {
                    gatewayInstance.close();
                    throw new IllegalStateException(String.format("Unable to start '%s' instance", instanceName));
                }
            else throw new InstantiationException(String.format("Unable to instantiate '%s' instance", instanceName));
        }

        @Override
        protected G createService(final Map<String, Object> identity,
                                  final Dictionary<String, ?> configuration,
                                  final RequiredService<?>... dependencies) throws Exception {
            final CMGatewayParser parser = getParser(dependencies);
            final String instanceName = parser.getInstanceName(configuration);
            @SuppressWarnings("unchecked")
            final GatewayConfiguration newConfig = getNewConfiguration(instanceName, getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies));
            if(newConfig == null)
                throw new IllegalStateException(String.format("Gateway %s cannot be created. Configuration not found.", instanceName));
            return createService(identity, instanceName, newConfig, dependencies);
        }

        @Override
        protected void cleanupService(final G gatewayInstance, final Map<String, ?> identity) throws IOException {
            gatewayInstance.close();
        }

        @Override
        protected void failedToUpdateService(final Logger logger,
                                             final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e) {
            logger.log(Level.SEVERE,
                    String.format("Unable to update gateway. Type: %s, instance: %s",
                            gatewayType,
                            servicePID),
                    e);
        }

        @Override
        protected void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to release gateway. Type: %s, instance: %s", gatewayType, servicePID),
                    e);
        }
    }

    private static void createIdentity(final String gatewayType,
                                       final String instanceName,
                                       final Map<String, Object> identity){
        identity.put(GATEWAY_TYPE_IDENTITY_PROPERTY, gatewayType);
        identity.put(GATEWAY_INSTANCE_IDENTITY_PROPERTY, instanceName);
    }

    /**
     * Represents activator for support service.
     * @param <T> Type of support service.
     * @since 2.0
     */
    protected interface SupportServiceActivator<T extends SupportService>{
        T activateService(final RequiredService<?>... dependencies) throws Exception;
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
    protected final static class SupportGatewayServiceManager<S extends SupportService, T extends S> extends ProvidedService<S, T>{
        private final SupportServiceActivator<T> activator;

        private SupportGatewayServiceManager(final Class<S> contract,
                                             final SupportServiceActivator<T> activator,
                                             final RequiredService<?>... dependencies) {
            super(contract, dependencies);
            this.activator = Objects.requireNonNull(activator);
        }

        @Override
        protected T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(GATEWAY_TYPE_IDENTITY_PROPERTY, getGatewayType());
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

    /**
     * Initializes a new instance of the gateway activator.
     * @param factory Gateway factory. Cannot be {@literal null}.
     * @param optionalServices Additional services exposed by gateway.
     */
    protected GatewayActivator(final GatewayFactory<G> factory,
                               final SupportGatewayServiceManager<?, ?>... optionalServices){
        this(factory, emptyArray(RequiredService[].class), optionalServices);
    }

    private GatewayActivator(final GatewayInstances<?> registry,
                             final SupportGatewayServiceManager<?, ?>[] optionalServices) {
        super(ObjectArrays.concat(new ServiceSubRegistryManager<?, ?>[]{registry}, optionalServices, ProvidedService.class));
    }

    /**
     * Initializes a new instance of the gateway activator.
     * @param factory Gateway factory. Cannot be {@literal null}.
     * @param gatewayDependencies Gateway-level dependencies.
     * @param optionalServices Additional services exposed by gateway.
     */
    protected GatewayActivator(final GatewayFactory<G> factory,
                               final RequiredService<?>[] gatewayDependencies,
                               final SupportGatewayServiceManager<?, ?>[] optionalServices) {
        this(new GatewayInstances<>(factory, gatewayDependencies), optionalServices);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportGatewayServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final SupportServiceActivator<T> factory,
                                                                                                                                                                        final RequiredService<?>... dependencies) {
        return new SupportGatewayServiceManager<>(ConfigurationEntityDescriptionProvider.class, factory, dependencies);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportGatewayServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final Supplier<T> factory) {
        return configurationDescriptor(dependencies -> factory.get());
    }

    public final String getGatewayType() {
        return Gateway.getGatewayType(Utils.getBundleContextOfObject(this).getBundle());
    }

    /**
     * Exposes additional gateway dependencies.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param dependencies A collection of dependencies to fill.
     */
    @MethodStub
    protected void addDependencies(final Collection<RequiredService<?>> dependencies){

    }

    /**
     * Initializes the library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     */
    @Override
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
        addDependencies(bundleLevelDependencies);
        start();
    }

    @MethodStub
    protected void start(){

    }

    /**
     * Activates this service library.
     * <p>
     * You should override this method and call this implementation at the first line using
     * <b>super</b> keyword.
     * </p>
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected final void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        activationProperties.publish(GATEWAY_TYPE_HOLDER, getGatewayType());
        activationProperties.publish(LOGGER_HOLDER, getLogger());
        getLogger().info(String.format("Activating gateway of type %s", getGatewayType()));
    }

    /**
     * Gets logger associated with this activator.
     * @return A logger associated with this activator.
     */
    @Override
    protected Logger getLogger(){
        return AbstractGateway.getLogger(getGatewayType());
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    @Override
    protected final void deactivate(final ActivationPropertyReader activationProperties) throws Exception {
        getLogger().info(String.format("Unloading gateway of type %s", getGatewayType()));
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.bytex.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        getLogger().log(Level.SEVERE, String.format("Unable to activate %s gateway instance",
                        getGatewayType()),
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
        getLogger().log(Level.SEVERE, String.format("Unable to deactivate %s gateway instance",
                        getGatewayType()),
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

    static String createFilter(final String gatewayType, final String filter){
        return filter == null || filter.isEmpty() ?
                String.format("(%s=%s)", GATEWAY_TYPE_IDENTITY_PROPERTY, gatewayType):
                String.format("(&(%s=%s)%s)", GATEWAY_TYPE_IDENTITY_PROPERTY, gatewayType, filter);
    }

    static String createFilter(final String gatewayInstance){
        return String.format("(%s=%s)", GATEWAY_INSTANCE_IDENTITY_PROPERTY, gatewayInstance);
    }

    private static String getGatewayInstance(final Dictionary<String, ?> identity){
        return Objects.toString(identity.get(GATEWAY_INSTANCE_IDENTITY_PROPERTY), "");
    }

    static String getGatewayInstance(final ServiceReference<Gateway> gatewayInstance) {
        return gatewayInstance != null ?
                getGatewayInstance(getProperties(gatewayInstance)) :
                "";
    }
}