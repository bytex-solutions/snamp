package com.itworks.snamp.adapters;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractBundleActivator;
import com.itworks.snamp.core.AbstractLoggableServiceLibrary;
import com.itworks.snamp.internal.semantics.MethodStub;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.util.*;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
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
public abstract class AbstractResourceAdapterActivator<TAdapter extends AbstractResourceAdapter> extends AbstractBundleActivator {
    /**
     * Represents name of the bundle manifest header that contains system name of the adapter.
     */
    public static final String ADAPTER_NAME_MANIFEST_HEADER = "SNAMP-Resource-Adapter";

    /**
     * Represents name of the adapter.
     */
    public final String adapterName;
    private final Logger logger;
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
        this.adapterName = adapterName;
        this.logger = loggerInstance != null ? loggerInstance : AbstractResourceAdapter.getLogger(adapterName);
        adapters = new HashMap<>(4);
    }

    /**
     * Gets logger associated with this resource adapter.
     * @return The logger associated with this resource adapter.
     */
    protected final Logger getLogger(){
        return logger;
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
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected final void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(new AbstractLoggableServiceLibrary.LoggerServiceDependency(logger));
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
     * Activates the bundle.
     * <p>
     * This method will be called when all bundle-level dependencies will be resolved.
     * </p>
     *
     * @param context              The execution context of the bundle being activated.
     * @param activationProperties A collection of bundle's activation properties to fill.
     * @param dependencies         A collection of resolved dependencies.
     * @throws Exception An exception occurred during activation.
     */
    @Override
    protected final void activate(final BundleContext context,
                            final ActivationPropertyPublisher activationProperties,
                            final RequiredService<?>... dependencies) throws Exception {
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
                    resourceAdapter.update(context);
                }
            }
    }

    private void deactivate() throws Exception{
        for(final TAdapter adapter: adapters.values())
            adapter.close();
    }

    /**
     * Deactivates the bundle.
     * <p>
     * This method will be called when at least one bundle-level dependency will be lost.
     * </p>
     *
     * @param context              The execution context of the bundle being deactivated.
     * @param activationProperties A collection of activation properties to read.
     * @throws Exception An exception occurred during bundle deactivation.
     */
    @Override
    protected final void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        deactivate();
    }

    /**
     * Stops the bundle.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception An exception occurred during bundle stopping.
     */
    @Override
    @MethodStub
    protected final void shutdown(final BundleContext context) throws Exception {
        if(getState() == ActivationState.ACTIVATED)
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
}
