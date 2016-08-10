package com.bytex.snamp.gateway;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMResourceAdapterParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.FrameworkService;
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
        G createInstance(final String adapterInstance,
                         final RequiredService<?>... dependencies) throws Exception;
    }

    private static final class ResourceAdapterRegistry<G extends AbstractGateway> extends ServiceSubRegistryManager<Gateway, G>{
        private final GatewayFactory<G> adapterFactory;
        /**
         * Represents name of the resource adapter.
         */
        protected final String adapterName;

        private ResourceAdapterRegistry(final String adapterName,
                                        final GatewayFactory<G> factory,
                                        final RequiredService<?>... dependencies) {
            super(Gateway.class,
                    ObjectArrays.<RequiredService>concat(dependencies, new SimpleDependency<>(ConfigurationManager.class)));
            this.adapterFactory = Objects.requireNonNull(factory, "factory is null.");
            this.adapterName = adapterName;
        }

        private ResourceAdapterRegistry(final GatewayFactory<G> factory,
                                       final RequiredService<?>... dependencies) {
            this(Gateway.getGatewayType(Utils.getBundleContextOfObject(factory).getBundle()), factory, dependencies);
        }

        @SuppressWarnings("unchecked")
        private static CMResourceAdapterParser getParser(final RequiredService<?>... dependencies){
            final ConfigurationManager configManager = getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
            assert configManager != null;
            final CMResourceAdapterParser parser = configManager.queryObject(CMResourceAdapterParser.class);
            assert parser != null;
            return parser;
        }

        @Override
        protected String getFactoryPID(final RequiredService<?>[] dependencies) {
            return getParser(dependencies).getAdapterFactoryPersistentID(adapterName);
        }

        @Override
        protected G update(final G adapter,
                           final Dictionary<String, ?> configuration,
                           final RequiredService<?>... dependencies) throws Exception {
            final CMResourceAdapterParser parser = getParser(dependencies);
            adapter.tryUpdate(parser.getAdapterParameters(configuration));
            return adapter;
        }

        @Override
        protected G createService(final Map<String, Object> identity,
                                  final Dictionary<String, ?> configuration,
                                  final RequiredService<?>... dependencies) throws Exception {
            final CMResourceAdapterParser parser = getParser(dependencies);
            final String instanceName = parser.getAdapterInstanceName(configuration);
            createIdentity(adapterName, instanceName, identity);
            final G resourceAdapter = adapterFactory.createInstance(instanceName, dependencies);
            if (resourceAdapter != null)
                if (resourceAdapter.tryStart(parser.getAdapterParameters(configuration))) {
                    return resourceAdapter;
                } else {
                    resourceAdapter.close();
                    throw new IllegalStateException(String.format("Unable to start '%s' instance", instanceName));
                }
            else throw new InstantiationException(String.format("Unable to instantiate '%s' instance", instanceName));
        }

        @Override
        protected void cleanupService(final G adapter, final Dictionary<String, ?> identity) throws IOException {
            adapter.close();
        }

        @Override
        protected void failedToUpdateService(final Logger logger,
                                             final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e) {
            logger.log(Level.SEVERE,
                    String.format("Unable to update adapter. Name: %s, instance: %s",
                            adapterName,
                            servicePID),
                    e);
        }

        @Override
        protected void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to release adapter. Name: %s, instance: %s", adapterName, servicePID),
                    e);
        }
    }

    private static void createIdentity(final String adapterName,
                                       final String instanceName,
                                       final Map<String, Object> identity){
        identity.put(GATEWAY_TYPE_IDENTITY_PROPERTY, adapterName);
        identity.put(GATEWAY_INSTANCE_IDENTITY_PROPERTY, instanceName);
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
    protected abstract static class SupportGatewayServiceManager<S extends FrameworkService, T extends S> extends ProvidedService<S, T>{
        private SupportGatewayServiceManager(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        abstract T activateService(final RequiredService<?>... dependencies);

        @Override
        protected final T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            identity.put(GATEWAY_TYPE_IDENTITY_PROPERTY, getGatewayType());
            return activateService(dependencies);
        }

        /**
         * Gets name of the adapter.
         * @return The name of the adapter.
         */
        private String getGatewayType(){
            return getActivationPropertyValue(GATEWAY_TYPE_HOLDER);
        }
    }

    /**
     * Initializes a new instance of the resource adapter activator.
     * @param factory Gateway factory. Cannot be {@literal null}.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected GatewayActivator(final GatewayFactory<G> factory,
                               final SupportGatewayServiceManager<?, ?>... optionalServices){
        this(factory, emptyArray(RequiredService[].class), optionalServices);
    }

    private GatewayActivator(final ResourceAdapterRegistry<?> registry,
                             final SupportGatewayServiceManager<?, ?>[] optionalServices) {
        super(ObjectArrays.concat(new ServiceSubRegistryManager<?, ?>[]{registry}, optionalServices, ProvidedService.class));
    }

    /**
     * Initializes a new instance of the resource adapter activator.
     * @param factory Resource adapter factory. Cannot be {@literal null}.
     * @param adapterDependencies Adapter-level dependencies.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected GatewayActivator(final GatewayFactory<G> factory,
                               final RequiredService<?>[] adapterDependencies,
                               final SupportGatewayServiceManager<?, ?>[] optionalServices) {
        this(new ResourceAdapterRegistry<>(factory, adapterDependencies), optionalServices);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportGatewayServiceManager<ConfigurationEntityDescriptionProvider, T> configurationDescriptor(final Supplier<T> factory) {
        return new SupportGatewayServiceManager<ConfigurationEntityDescriptionProvider, T>(ConfigurationEntityDescriptionProvider.class) {
            @Override
            T activateService(final RequiredService<?>... dependencies) {
                return factory.get();
            }
        };
    }

    public final String getGatewayType() {
        return Gateway.getGatewayType(Utils.getBundleContextOfObject(this).getBundle());
    }

    /**
     * Exposes additional adapter dependencies.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param dependencies A collection of dependencies to fill.
     */
    @SuppressWarnings("UnusedParameters")
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
        getLogger().info(String.format("Activating resource gateway of type %s", getGatewayType()));
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
        getLogger().log(Level.SEVERE, String.format("Unable to activate %s resource adapter instance",
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
        getLogger().log(Level.SEVERE, String.format("Unable to deactivate %s resource adapter instance",
                        getGatewayType()),
                e);
    }

    private static List<Bundle> getGatewayBundles(final BundleContext context){
        return Arrays.stream(context.getBundles())
                .filter(Gateway::isGatewayBundle)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    static List<Bundle> getGatewayBundles(final BundleContext context, final String adapterName) {
        return Arrays.stream(context.getBundles())
                .filter(bnd -> Gateway.getGatewayType(bnd).equals(adapterName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Stops all managed resource gateway loaded into the current OSGi environment.
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
     * Stops the specified managed resource adapter.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param adapterName The name of the adapter to stop.
     * @return {@literal true}, if bundle with the specified adapter exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop gateway.
     */
    public static boolean disableGateway(final BundleContext context, final String adapterName) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getGatewayBundles(context, adapterName)) {
            bnd.stop();
            success = true;
        }
        return success;
    }

    /**
     * Starts all managed resource gateway loaded into the current OSGi environment.
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
     * Starts the specified managed resource adapter.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param adapterName The name of the adapter to start.
     * @return {@literal true}, if bundle with the specified adapter exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start adapter.
     */
    public static boolean enableGateway(final BundleContext context, final String adapterName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getGatewayBundles(context, adapterName)) {
            bnd.start();
            success = true;
        }
        return success;
    }


    /**
     * Gets a collection of installed gateway (types).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed adapter (system names).
     */
    public static Collection<String> getInstalledGateways(final BundleContext context) {
        final Collection<Bundle> candidates = getGatewayBundles(context);
        return candidates.stream()
                .map(Gateway::getGatewayType)
                .filter(name -> !isNullOrEmpty(name))
                .collect(Collectors.toCollection(HashSet::new));
    }

    static String createFilter(final String adapterName, final String filter){
        return filter == null || filter.isEmpty() ?
                String.format("(%s=%s)", GATEWAY_TYPE_IDENTITY_PROPERTY, adapterName):
                String.format("(&(%s=%s)%s)", GATEWAY_TYPE_IDENTITY_PROPERTY, adapterName, filter);
    }

    static String createFilter(final String adapterInstanceName){
        return String.format("(%s=%s)", GATEWAY_INSTANCE_IDENTITY_PROPERTY, adapterInstanceName);
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
