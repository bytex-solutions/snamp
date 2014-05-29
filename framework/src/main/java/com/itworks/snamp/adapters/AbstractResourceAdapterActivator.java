package com.itworks.snamp.adapters;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractLoggableServiceLibrary;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.semantics.MethodStub;
import org.osgi.framework.*;

import java.util.*;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.*;
import static java.util.Map.Entry;

/**
 * Represents lifetime manager for managed resource adapter.
 * <p>
 *     This class is a recommendation for the resource adapter implementation. Of course,
 *     you can write your own {@link org.osgi.framework.BundleActivator} implementation
 *     and consumes instances of {@link com.itworks.snamp.connectors.ManagedResourceConnector} services.
 * </p>
 * @param <TAdapter> Type of the resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractResourceAdapterActivator<TAdapter extends AbstractResourceAdapter> extends AbstractLoggableServiceLibrary {
    /**
     * Represents name of the bundle manifest header that contains system name of the adapter.
     */
    public static final String ADAPTER_NAME_MANIFEST_HEADER = "SNAMP-Resource-Adapter";


    private static final String ADAPTER_NAME_IDENTITY_PROPERTY = "adapterName";
    private static final ActivationProperty<String> ADAPTER_NAME_HOLDER = defineActivationProperty(String.class);

    /**
     * Represents a holder for connector configuration descriptor.
     * @param <T> Type of the configuration descriptor implementation.
     * @author Roman Sakno
     * @since 1.0
     */
    protected abstract static class ConfigurationEntityDescriptionProviderHolder<T extends ConfigurationEntityDescriptionProvider> extends LoggableProvidedService<ConfigurationEntityDescriptionProvider, T>{

        /**
         * Initializes a new holder for the provided service.
         *
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected ConfigurationEntityDescriptionProviderHolder(final RequiredService<?>... dependencies) {
            super(ConfigurationEntityDescriptionProvider.class, dependencies);
        }

        /**
         * Gets name of the resource connector.
         * @return The name of the resource connector.
         */
        protected final String getAdapterName(){
            return getActivationPropertyValue(ADAPTER_NAME_HOLDER);
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
            identity.put(ADAPTER_NAME_IDENTITY_PROPERTY, getAdapterName());
            return createConfigurationDescriptionProvider(dependencies);
        }
    }

    /**
     * Represents name of the adapter.
     */
    public final String adapterName;
    private final Map<String, TAdapter> adapters;

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     * @param adapterName The name of the adapter.
     */
    protected AbstractResourceAdapterActivator(final String adapterName){
        this(adapterName, AbstractResourceAdapter.getLogger(adapterName));
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     * @param adapterName The name of the adapter.
     * @param loggerInstance The logger associated with the adapter.
     */
    protected AbstractResourceAdapterActivator(final String adapterName, final Logger loggerInstance){
        super(loggerInstance != null ? loggerInstance : AbstractResourceAdapter.getLogger(adapterName));
        this.adapterName = adapterName;
        adapters = new HashMap<>(4);
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     * @param adapterName The name of the adapter.
     * @param descriptionProvider A service that exposes configuration schema of the adapter. Cannot be {@literal null}.
     */
    protected AbstractResourceAdapterActivator(final String adapterName, final ConfigurationEntityDescriptionProviderHolder<?> descriptionProvider){
        this(adapterName, AbstractResourceAdapter.getLogger(adapterName), descriptionProvider);
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     * @param adapterName The name of the adapter.
     * @param loggerInstance The logger associated with the adapter.
     * @param descriptionProvider A service that exposes configuration schema of the adapter. Cannot be {@literal null}.
     */
    protected AbstractResourceAdapterActivator(final String adapterName, final Logger loggerInstance, final ConfigurationEntityDescriptionProviderHolder<?> descriptionProvider){
        super(loggerInstance != null ? loggerInstance : AbstractResourceAdapter.getLogger(adapterName), descriptionProvider);
        this.adapterName = adapterName;
        adapters = new HashMap<>(4);
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
     * <p>
     * You should override this method and call this implementation at the first line using
     * <b>super keyword</b>.
     * </p>
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     * @throws Exception An error occurred during bundle initialization.
     */
    @Override
    protected final void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationManager.class));
        addDependencies(bundleLevelDependencies);
    }

    /**
     * Initializes a new instance of the resource adapter.
     * @param parameters A collection of initialization parameters.
     * @param resources A collection of managed resources to be exposed via adapter.
     * @return A new instance of the adapter.
     */
    protected abstract TAdapter createAdapter(final Map<String, String> parameters, final Collection<ManagedResourceConfiguration> resources);

    /**
     * Activates this service library.
     * <p>
     * You should override this method and call this implementation at the first line using
     * <b>super keyword</b>.
     * </p>
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected final void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        final ConfigurationManager configManager =
                getDependency(RequiredServiceAccessor.class, ConfigurationManager.class, dependencies);
        final AgentConfiguration config = configManager.getCurrentConfiguration();
        //select compliant adapters
        for(final Entry<String, ResourceAdapterConfiguration> adapter: config.getResourceAdapters().entrySet())
            if(Objects.equals(adapter.getValue().getAdapterName(), adapterName)) {
                final TAdapter resourceAdapter = createAdapter(adapter.getValue().getHostingParams(), config.getManagedResources().values());
                if(resourceAdapter != null) {
                    adapters.put(adapter.getKey(), resourceAdapter);
                    //update the adapter with dependencies
                    resourceAdapter.update(Utils.getBundleContextByObject(this));
                }
            }
        activationProperties.publish(ADAPTER_NAME_HOLDER, adapterName);
    }

    private void deactivate() throws Exception{
        for(final TAdapter adapter: adapters.values())
            adapter.close();
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    @Override
    protected final void deactivate(final ActivationPropertyReader activationProperties) throws Exception {
        deactivate();
    }

    /**
     * Determines whether the specified bundle provides implementation
     * of the managed resource adapter.
     * @param bnd The bundle to check.
     * @return {@literal true}, if the specified bundle provides implementation
     * of the managed resource adapter.
     */
    public static boolean isResourceAdapterBundle(final Bundle bnd){
        return bnd != null && bnd.getHeaders().get(ADAPTER_NAME_MANIFEST_HEADER) != null;
    }

    private static Collection<Bundle> getResourceAdapterBundles(final BundleContext context){
        final Bundle[] bundles = context.getBundles();
        final Collection<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(isResourceAdapterBundle(bnd)) result.add(bnd);
        return result;
    }

    private static Collection<Bundle> getResourceAdapterBundles(final BundleContext context, final String connectorName){
        final Bundle[] bundles = context.getBundles();
        final Collection<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(Objects.equals(bnd.getHeaders().get(ADAPTER_NAME_MANIFEST_HEADER), connectorName))
                result.add(bnd);
        return result;
    }

    /**
     * Stops all managed resource adapters loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop adapters.
     */
    public static void stopResourceAdapters(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalStateException("context is null.");
        for(final Bundle bnd: getResourceAdapterBundles(context))
            bnd.stop();
    }

    /**
     * Stops the specified managed resource adapter.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param adapterName The name of the adapter to stop.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop adapters.
     */
    public static void stopResourceAdapter(final BundleContext context, final String adapterName) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceAdapterBundles(context, adapterName))
            bnd.stop();
    }

    /**
     * Starts all managed resource adapters loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @throws BundleException Unable to start adapters.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static void startResourceAdapters(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceAdapterBundles(context))
            bnd.start();
    }

    /**
     * Starts the specified managed resource adapter.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param adapterName The name of the adapter to start.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start adapter.
     */
    public static void startResourceConnector(final BundleContext context, final String adapterName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceAdapterBundles(context, adapterName))
            bnd.start();
    }

    /**
     * Gets configuration descriptor for the specified adapter.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @param adapterName The name of the adapter.
     * @param configurationEntity Type of the configuration entity.
     * @param <T> Type of the configuration entity.
     * @return Configuration entity descriptor; or {@literal null}, if configuration description is not supported.
     */
    public static <T extends ConfigurationEntity> ConfigurationEntityDescription<T> getConfigurationEntityDescriptor(final BundleContext context,
                                                                                                                                        final String adapterName,
                                                                                                                                        final Class<T> configurationEntity){
        if(context == null || configurationEntity == null) return null;
        ServiceReference<?>[] refs;
        try {
            refs = context.getAllServiceReferences(ConfigurationEntityDescriptionProvider.class.getName(), String.format("(%s=%s)", ADAPTER_NAME_IDENTITY_PROPERTY, adapterName));
        }
        catch (final InvalidSyntaxException e) {
            refs = null;
        }
        for(final ServiceReference<?> providerRef: refs != null ? refs : new ServiceReference<?>[0])
            try{
                final ConfigurationEntityDescriptionProvider provider = (ConfigurationEntityDescriptionProvider)context.getService(providerRef);
                final ConfigurationEntityDescription<T> description = provider.getDescription(configurationEntity);
                if(description != null) return description;
            }
            finally {
                context.ungetService(providerRef);
            }
        return null;
    }
}
