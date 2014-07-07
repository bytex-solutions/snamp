package com.itworks.snamp.adapters;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.core.AbstractLoggableServiceLibrary;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.semantics.MethodStub;
import com.itworks.snamp.licensing.LicenseLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.IteratorUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.util.*;
import java.util.logging.Level;
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
public abstract class AbstractResourceAdapterActivator<TAdapter extends AbstractResourceAdapter> extends AbstractLoggableServiceLibrary {
    /**
     * Represents name of the bundle manifest header that contains system name of the adapter.
     */
    public static final String ADAPTER_NAME_MANIFEST_HEADER = "SNAMP-Resource-Adapter";


    private static final String ADAPTER_NAME_IDENTITY_PROPERTY = ADAPTER_NAME_MANIFEST_HEADER;
    private static final ActivationProperty<String> ADAPTER_NAME_HOLDER = defineActivationProperty(String.class);

    /**
     * Represents superclass for all optional adapter-related service factories.
     * You cannot derive from this class directly.
     * @param <S> Type of the adapter-related service contract.
     * @param <T> Type of the adapter-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     * @see com.itworks.snamp.adapters.AbstractResourceAdapterActivator.ConfigurationEntityDescriptionProviderHolder
     * @see com.itworks.snamp.adapters.AbstractResourceAdapterActivator.LicensingDescriptionServiceProvider
     */
    protected abstract static class OptionalAdapterServiceProvider<S extends FrameworkService, T extends S> extends LoggableProvidedService<S, T>{

        private OptionalAdapterServiceProvider(final Class<S> contract, final RequiredService<?>... dependencies) {
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
     * Represents maintenance service provider.
     * @param <T> Type of the maintenance service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class MaintenanceServiceProvider<T extends Maintainable> extends OptionalAdapterServiceProvider<Maintainable,T>{

        protected MaintenanceServiceProvider(final RequiredService<?>... dependencies) {
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
            identity.put(ADAPTER_NAME_IDENTITY_PROPERTY, getAdapterName());
            return createMaintenanceService(dependencies);
        }
    }

    private static final class AdapterLicensingDescriptorService<L extends LicenseLimitations> extends AbstractAggregator implements LicensingDescriptionService {
        private final Logger logger;
        private final LicenseReader licenseReader;
        private final Class<L> descriptor;
        private final Factory<L> fallbackFactory;

        public AdapterLicensingDescriptorService(final LicenseReader reader,
                                                 final Class<L> descriptor,
                                                 final Factory<L> fallbackFactory,
                                                 final Logger l){
            this.licenseReader = reader;
            this.descriptor = descriptor;
            this.fallbackFactory = fallbackFactory;
            this.logger = l;
        }

        @Override
        public Logger getLogger() {
            return logger;
        }

        /**
         * Gets a read-only collection of license limitations.
         *
         * @return A read-only collection of license limitations.
         */
        @Override
        public Collection<String> getLimitations() {
            return IteratorUtils.toList(licenseReader.getLimitations(descriptor, fallbackFactory).iterator());
        }

        /**
         * Gets human-readable description of the specified limitation.
         *
         * @param limitationName The system name of the limitation.
         * @param loc            The locale of the description. May be {@literal null}.
         * @return The description of the limitation.
         */
        @Override
        public String getDescription(final String limitationName, final Locale loc) {
            final LicenseLimitations.Limitation<?> lim =  licenseReader.getLimitations(descriptor, fallbackFactory).getLimitation(limitationName);
            return lim != null ? lim.getDescription(loc) : "";
        }
    }

    protected final static class LicensingDescriptionServiceProvider<L extends LicenseLimitations> extends OptionalAdapterServiceProvider<LicensingDescriptionService, AdapterLicensingDescriptorService>{
        private final Factory<L> fallbackFactory;
        private final Class<L> descriptor;

        public LicensingDescriptionServiceProvider(final Class<L> limitationsDescriptor,
                                                   final Factory<L> fallbackFactory) {
            super(LicensingDescriptionService.class, new SimpleDependency<>(LicenseReader.class));
            if(fallbackFactory == null) throw new IllegalArgumentException("fallbackFactory is null.");
            else if(limitationsDescriptor == null) throw new IllegalArgumentException("limitationsDescriptor is null.");
            else{
                this.fallbackFactory = fallbackFactory;
                this.descriptor = limitationsDescriptor;
            }
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected final AdapterLicensingDescriptorService activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            identity.put(ADAPTER_NAME_IDENTITY_PROPERTY, getAdapterName());
            return new AdapterLicensingDescriptorService(getDependency(SimpleDependency.class, LicenseReader.class, dependencies),
                    descriptor,
                    fallbackFactory,
                    getLogger());
        }
    }

    /**
     * Represents a holder for connector configuration descriptor.
     * @param <T> Type of the configuration descriptor implementation.
     * @author Roman Sakno
     * @since 1.0
     */
    protected abstract static class ConfigurationEntityDescriptionProviderHolder<T extends ConfigurationEntityDescriptionProvider> extends OptionalAdapterServiceProvider<ConfigurationEntityDescriptionProvider, T>{

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
     * @param optionalServices Additional services exposed by adapter.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected AbstractResourceAdapterActivator(final String adapterName, final OptionalAdapterServiceProvider<?, ?>... optionalServices){
        this(adapterName, AbstractResourceAdapter.getLogger(adapterName), optionalServices);
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     * @param adapterName The name of the adapter.
     * @param loggerInstance The logger associated with the adapter.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected AbstractResourceAdapterActivator(final String adapterName, final Logger loggerInstance, final OptionalAdapterServiceProvider<?, ?>... optionalServices){
        super(loggerInstance != null ? loggerInstance : AbstractResourceAdapter.getLogger(adapterName), optionalServices);
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
     * @param dependencies A collection of dependencies used by adapter.
     * @return A new instance of the adapter.
     * @throws java.lang.Exception Unable to instantiate resource adapter.
     */
    protected abstract TAdapter createAdapter(final Map<String, String> parameters,
                                              final Map<String, ManagedResourceConfiguration> resources,
                                              final RequiredService<?>... dependencies) throws Exception;

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
                final TAdapter resourceAdapter = createAdapter(adapter.getValue().getHostingParams(), config.getManagedResources(), dependencies);
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
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.itworks.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        getLogger().log(Level.SEVERE, String.format("Unable to activate %s resource adapter instance", adapterName), e);
    }

    /**
     * Handles an exception thrown by {@link } method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        getLogger().log(Level.SEVERE, String.format("Unable to deactivate %s resource adapter instance.", adapterName), e);
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

    private static List<Bundle> getResourceAdapterBundles(final BundleContext context){
        final Bundle[] bundles = context.getBundles();
        final List<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(isResourceAdapterBundle(bnd)) result.add(bnd);
        return result;
    }

    static List<Bundle> getResourceAdapterBundles(final BundleContext context, final String adapterName){
        final Bundle[] bundles = context.getBundles();
        final List<Bundle> result = new ArrayList<>(bundles.length);
        for(final Bundle bnd: bundles)
            if(Objects.equals(bnd.getHeaders().get(ADAPTER_NAME_MANIFEST_HEADER), adapterName))
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
    public static void startResourceAdapter(final BundleContext context, final String adapterName) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        for(final Bundle bnd: getResourceAdapterBundles(context, adapterName))
            bnd.start();
    }

    /**
     * Gets a collection of installed adapters (system names).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed adapter (system names).
     */
    public static Collection<String> getInstalledResourceAdapters(final BundleContext context){
        final Collection<Bundle> candidates = getResourceAdapterBundles(context);
        final Collection<String> systemNames = new ArrayList<>(candidates.size());
        for(final Bundle bnd: candidates)
            systemNames.add(bnd.getHeaders().get(ADAPTER_NAME_MANIFEST_HEADER));
        return systemNames;
    }

    static String createFilter(final String adapterName, final String filter){
        return filter == null || filter.isEmpty() ?
                String.format("(%s=%s)", ADAPTER_NAME_IDENTITY_PROPERTY, adapterName):
                String.format("(&(%s=%s)%s)", ADAPTER_NAME_IDENTITY_PROPERTY, adapterName, filter);
    }
}
