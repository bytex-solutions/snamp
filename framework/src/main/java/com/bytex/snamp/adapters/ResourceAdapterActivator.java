package com.bytex.snamp.adapters;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMResourceAdapterParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.management.Maintainable;
import com.google.common.collect.ObjectArrays;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.adapters.ResourceAdapter.ADAPTER_NAME_MANIFEST_HEADER;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents lifetime manager for managed resource adapter.
 * <p>
 *     This class is a recommendation for the resource adapter implementation. Of course,
 *     you can write your own {@link org.osgi.framework.BundleActivator} implementation
 *     and consumes instances of {@link com.bytex.snamp.connectors.ManagedResourceConnector} services.
 * </p>
 * @param <TAdapter> Type of the resource adapter.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ResourceAdapterActivator<TAdapter extends AbstractResourceAdapter> extends AbstractServiceLibrary {
    private static final String ADAPTER_INSTANCE_IDENTITY_PROPERTY = "instanceName";
    private static final ActivationProperty<String> ADAPTER_NAME_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents a factory responsible for creating instances of resource adapters.
     * @param <TAdapter> Type of the adapter implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    protected interface ResourceAdapterFactory<TAdapter extends ResourceAdapter>{
        TAdapter createAdapter(final String adapterInstance,
                               final RequiredService<?>... dependencies) throws Exception;
    }

    private static final class ResourceAdapterRegistry<TAdapter extends AbstractResourceAdapter> extends ServiceSubRegistryManager<ResourceAdapter, TAdapter>{
        private final ResourceAdapterFactory<TAdapter> adapterFactory;
        /**
         * Represents name of the resource adapter.
         */
        protected final String adapterName;

        private ResourceAdapterRegistry(final String adapterName,
                                        final ResourceAdapterFactory<TAdapter> factory,
                                        final RequiredService<?>... dependencies) {
            super(ResourceAdapter.class,
                    ObjectArrays.<RequiredService>concat(dependencies, new SimpleDependency<>(ConfigurationManager.class)));
            this.adapterFactory = Objects.requireNonNull(factory, "factory is null.");
            this.adapterName = adapterName;
        }

        private ResourceAdapterRegistry(final ResourceAdapterFactory<TAdapter> factory,
                                       final RequiredService<?>... dependencies) {
            this(getAdapterName(factory), factory, dependencies);
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
        protected TAdapter update(final TAdapter adapter,
                                  final Dictionary<String, ?> configuration,
                                  final RequiredService<?>... dependencies) throws Exception {
            final CMResourceAdapterParser parser = getParser(dependencies);
            adapter.tryUpdate(parser.getAdapterParameters(configuration));
            return adapter;
        }

        @Override
        protected TAdapter createService(final Map<String, Object> identity,
                                         final Dictionary<String, ?> configuration,
                                         final RequiredService<?>... dependencies) throws Exception {
            final CMResourceAdapterParser parser = getParser(dependencies);
            final String instanceName = parser.getAdapterInstanceName(configuration);
            createIdentity(adapterName, instanceName, identity);
            final TAdapter resourceAdapter = adapterFactory.createAdapter(instanceName, dependencies);
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
        protected void cleanupService(final TAdapter adapter, final Dictionary<String, ?> identity) throws IOException {
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
        identity.put(ADAPTER_NAME_MANIFEST_HEADER, adapterName);
        identity.put(ADAPTER_INSTANCE_IDENTITY_PROPERTY, instanceName);
    }

    /**
     * Represents superclass for all optional adapter-related service factories.
     * You cannot derive from this class directly.
     * @param <S> Type of the adapter-related service contract.
     * @param <T> Type of the adapter-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     * @see ResourceAdapterActivator.ConfigurationEntityDescriptionManager
     */
    protected abstract static class SupportAdapterServiceManager<S extends FrameworkService, T extends S> extends ProvidedService<S, T>{

        private SupportAdapterServiceManager(final Class<S> contract, final RequiredService<?>... dependencies) {
            super(contract, dependencies);
        }

        /**
         * Gets name of the adapter.
         * @return The name of the adapter.
         */
        protected final String getAdapterName(){
            return getActivationPropertyValue(ADAPTER_NAME_HOLDER);
        }
    }

    /**
     * Represents maintenance service manager.
     * @param <T> Type of the maintenance service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    protected static abstract class MaintenanceServiceManager<T extends Maintainable> extends SupportAdapterServiceManager<Maintainable,T> {

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
            identity.put(ADAPTER_NAME_MANIFEST_HEADER, getAdapterName());
            return createMaintenanceService(dependencies);
        }
    }

    /**
     * Represents a holder for connector configuration descriptor.
     * @param <T> Type of the configuration descriptor implementation.
     * @author Roman Sakno
     * @since 1.0
     */
    protected abstract static class ConfigurationEntityDescriptionManager<T extends ConfigurationEntityDescriptionProvider> extends SupportAdapterServiceManager<ConfigurationEntityDescriptionProvider, T> {

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
            identity.put(ADAPTER_NAME_MANIFEST_HEADER, getAdapterName());
            return createConfigurationDescriptionProvider(dependencies);
        }
    }

    /**
     * Initializes a new instance of the resource adapter activator.
     * @param factory Resource adapter factory. Cannot be {@literal null}.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected ResourceAdapterActivator(final ResourceAdapterFactory<TAdapter> factory,
                                       final SupportAdapterServiceManager<?, ?>... optionalServices){
        this(factory, emptyArray(RequiredService[].class), optionalServices);
    }

    private ResourceAdapterActivator(final ResourceAdapterRegistry<?> registry,
                                     final SupportAdapterServiceManager<?, ?>[] optionalServices) {
        super(ObjectArrays.concat(new ServiceSubRegistryManager<?, ?>[]{registry}, optionalServices, ProvidedService.class));
    }

    /**
     * Initializes a new instance of the resource adapter activator.
     * @param factory Resource adapter factory. Cannot be {@literal null}.
     * @param adapterDependencies Adapter-level dependencies.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected ResourceAdapterActivator(final ResourceAdapterFactory<TAdapter> factory,
                                       final RequiredService<?>[] adapterDependencies,
                                       final SupportAdapterServiceManager<?, ?>[] optionalServices) {
        this(new ResourceAdapterRegistry<>(factory, adapterDependencies), optionalServices);
    }

    private static String getAdapterName(final ResourceAdapterFactory<?> factory){
        return getAdapterName(FrameworkUtil.getBundle(factory.getClass()));
    }

    public final String getAdapterName(){
        return getAdapterName(FrameworkUtil.getBundle(getClass()));
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
        activationProperties.publish(ADAPTER_NAME_HOLDER, getAdapterName());
        activationProperties.publish(LOGGER_HOLDER, getLogger());
        getLogger().info(String.format("Activating resource adapters of type %s", getAdapterName()));
    }

    /**
     * Gets logger associated with this activator.
     * @return A logger associated with this activator.
     */
    @Override
    protected Logger getLogger(){
        return AbstractResourceAdapter.getLogger(getAdapterName());
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    @Override
    protected final void deactivate(final ActivationPropertyReader activationProperties) throws Exception {
        getLogger().info(String.format("Unloading adapters of type %s", getAdapterName()));
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
                        getAdapterName()),
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
                        getAdapterName()),
                e);
    }

    /**
     * Determines whether the specified bundle provides implementation
     * of the managed resource adapter.
     * @param bnd The bundle to check.
     * @return {@literal true}, if the specified bundle provides implementation
     * of the managed resource adapter.
     */
    public static boolean isResourceAdapterBundle(final Bundle bnd){
        return AbstractResourceAdapter.isResourceAdapterBundle(bnd);
    }

    private static List<Bundle> getResourceAdapterBundles(final BundleContext context){
        return Arrays.stream(context.getBundles())
                .filter(ResourceAdapterActivator::isResourceAdapterBundle)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    static List<Bundle> getResourceAdapterBundles(final BundleContext context, final String adapterName) {
        return Arrays.stream(context.getBundles())
                .filter(bnd -> Objects.equals(bnd.getHeaders().get(ADAPTER_NAME_MANIFEST_HEADER), adapterName))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Stops all managed resource adapters loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @return Number of stopped bundles.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop adapters.
     */
    public static int stopResourceAdapters(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalStateException("context is null.");
        int count = 0;
        for(final Bundle bnd: getResourceAdapterBundles(context)) {
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
     * @throws BundleException Unable to stop adapters.
     */
    public static boolean stopResourceAdapter(final BundleContext context, final String adapterName) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getResourceAdapterBundles(context, adapterName)) {
            bnd.stop();
            success = true;
        }
        return success;
    }

    /**
     * Starts all managed resource adapters loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @return Number of started bundles with adapters.
     * @throws BundleException Unable to start adapters.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static int startResourceAdapters(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        int count = 0;
        for(final Bundle bnd: getResourceAdapterBundles(context)) {
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
    public static boolean startResourceAdapter(final BundleContext context, final String adapterName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getResourceAdapterBundles(context, adapterName)) {
            bnd.start();
            success = true;
        }
        return success;
    }

    private static String getAdapterName(final Bundle bnd){
        return AbstractResourceAdapter.getAdapterName(bnd);
    }

    /**
     * Gets a collection of installed adapters (system names).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed adapter (system names).
     */
    public static Collection<String> getInstalledResourceAdapters(final BundleContext context) {
        final Collection<Bundle> candidates = getResourceAdapterBundles(context);
        return candidates.stream()
                .map(ResourceAdapterActivator::getAdapterName)
                .filter(name -> !isNullOrEmpty(name))
                .collect(Collectors.toCollection(HashSet::new));
    }

    static String createFilter(final String adapterName, final String filter){
        return filter == null || filter.isEmpty() ?
                String.format("(%s=%s)", ADAPTER_NAME_MANIFEST_HEADER, adapterName):
                String.format("(&(%s=%s)%s)", ADAPTER_NAME_MANIFEST_HEADER, adapterName, filter);
    }

    static String createFilter(final String adapterInstanceName){
        return String.format("(%s=%s)", ADAPTER_INSTANCE_IDENTITY_PROPERTY, adapterInstanceName);
    }

    private static String getAdapterInstanceName(final Dictionary<String, ?> identity){
        return Objects.toString(identity.get(ADAPTER_INSTANCE_IDENTITY_PROPERTY), "");
    }

    static String getAdapterInstanceName(final ServiceReference<ResourceAdapter> adapterInstance) {
        return adapterInstance != null ?
                getAdapterInstanceName(getProperties(adapterInstance)) :
                "";
    }
}
