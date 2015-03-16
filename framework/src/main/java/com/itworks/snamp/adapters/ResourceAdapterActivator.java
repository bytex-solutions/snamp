package com.itworks.snamp.adapters;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.LicenseLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class ResourceAdapterActivator<TAdapter extends AbstractResourceAdapter> extends AbstractServiceLibrary {
    /**
     * Represents name of the bundle manifest header that contains system name of the adapter.
     */
    public static final String ADAPTER_NAME_MANIFEST_HEADER = "SNAMP-Resource-Adapter";


    private static final String ADAPTER_NAME_IDENTITY_PROPERTY = ADAPTER_NAME_MANIFEST_HEADER;
    private static final ActivationProperty<String> ADAPTER_NAME_HOLDER = defineActivationProperty(String.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class);

    /**
     * Represents a factory responsible for creating instances of resource adapters.
     * @param <TAdapter> Type of the adapter implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ResourceAdapterFactory<TAdapter extends ResourceAdapter>{
        TAdapter createAdapter(final String adapterInstance,
                               final RequiredService<?>... dependencies) throws Exception;
    }

    private static final class ResourceAdapterRegistry<TAdapter extends AbstractResourceAdapter> extends DynamicServiceManager<TAdapter>{
        private final ResourceAdapterFactory<TAdapter> adapterFactory;
        /**
         * Represents name of the resource adapter.
         */
        protected final String adapterName;

        private ResourceAdapterRegistry(final String adapterName,
                                        final ResourceAdapterFactory<TAdapter> factory,
                                        final RequiredService<?>... dependencies){
            super(PersistentConfigurationManager.getAdapterFactoryPersistentID(adapterName), dependencies);
            this.adapterFactory = Objects.requireNonNull(factory, "factory is null.");
            this.adapterName = adapterName;
        }

        private BundleContext getBundleContext(){
            return Utils.getBundleContextByObject(adapterFactory);
        }

        private OSGiLoggingContext getLoggingContext() {
            Logger logger = getActivationPropertyValue(LOGGER_HOLDER);
            if (logger == null)
                logger = AbstractResourceAdapter.getLogger(adapterName);
            return OSGiLoggingContext.get(logger, getBundleContext());
        }

        /**
         * Automatically invokes by SNAMP when the dynamic service should be updated with
         * a new configuration.
         *
         * @param adapter      The service to be updated.
         * @param configuration A new configuration of the service.
         * @param dependencies  A collection of dependencies required for the service.
         * @return An updated service.
         * @throws Exception                                  Unable to create new service or update the existing service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @Override
        protected TAdapter updateService(final TAdapter adapter, final Dictionary<String, ?> configuration, final RequiredService<?>... dependencies) throws Exception {
            adapter.tryUpdate(PersistentConfigurationManager.getAdapterParameters(configuration));
            return adapter;
        }

        /**
         * Automatically invokes by SNAMP when the new dynamic service should be created.
         *
         * @param servicePID    The persistent identifier associated with a newly created service.
         * @param configuration A new configuration of the service.
         * @param dependencies  A collection of dependencies required for the newly created service.
         * @return A new instance of the service.
         * @throws Exception                                  Unable to instantiate the service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @Override
        protected TAdapter activateService(final String servicePID,
                                           final Dictionary<String, ?> configuration,
                                           final RequiredService<?>... dependencies) throws Exception {
            final TAdapter resourceAdapter = adapterFactory.createAdapter(PersistentConfigurationManager.getAdapterInstanceName(configuration),
                    dependencies);
            if (resourceAdapter != null && resourceAdapter.tryStart(adapterName, PersistentConfigurationManager.getAdapterParameters(configuration)))
                ManagedResourceConnectorClient.addResourceListener(getBundleContext(),
                        resourceAdapter);
            else try(final OSGiLoggingContext logger = getLoggingContext()){
                logger.severe("Adapter is not started.");
            }
            return resourceAdapter;
        }

        /**
         * Automatically invokes by SNAMP when service is disposing.
         *
         * @param adapter   A service to dispose.
         * @param bundleStop {@literal true}, if disposing is initiated by bundle stop operation; otherwise, {@literal false}.
         * @throws Exception Unable to dispose the service.
         */
        @Override
        protected void dispose(final TAdapter adapter, final boolean bundleStop) throws Exception {
            try {
                getBundleContext().removeServiceListener(adapter);
            }
            finally {
                adapter.close();
            }
        }

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])} failed.
         *
         * @param servicePID    The persistent identifier associated with the service.
         * @param configuration The configuration of the service.
         * @param e             An exception occurred when updating service.
         */
        @Override
        protected void failedToUpdateService(final String servicePID, final Dictionary<String, ?> configuration, final Exception e) {
            try(final OSGiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to update adapter. Name: %s, instance: %s. Context: %s",
                            adapterName,
                            PersistentConfigurationManager.getAdapterInstanceName(configuration),
                            LogicalOperation.current()),
                        e);
            }
        }

        /**
         * Logs error details when {@link #dispose(Object, boolean)} failed.
         *
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e          An exception occurred when disposing service.
         */
        @Override
        protected void failedToCleanupService(final String servicePID, final Exception e) {
            try(final OSGiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to release adapter. Name: %s. Context: %s",
                        LogicalOperation.current(),
                        adapterName), e);
            }
        }
    }

    /**
     * Represents superclass for all optional adapter-related service factories.
     * You cannot derive from this class directly.
     * @param <S> Type of the adapter-related service contract.
     * @param <T> Type of the adapter-related service implementation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     * @see ResourceAdapterActivator.ConfigurationEntityDescriptionManager
     * @see ResourceAdapterActivator.LicensingDescriptionServiceManager
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
     * @version 1.0
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
            identity.put(ADAPTER_NAME_IDENTITY_PROPERTY, getAdapterName());
            return createMaintenanceService(dependencies);
        }
    }

    private static final class AdapterLicensingDescriptorService<L extends LicenseLimitations> extends AbstractAggregator implements LicensingDescriptionService {
        private final LicenseReader licenseReader;
        private final Class<L> descriptor;
        private final Supplier<L> fallbackFactory;

        public AdapterLicensingDescriptorService(final LicenseReader reader,
                                                 final Class<L> descriptor,
                                                 final Supplier<L> fallbackFactory){
            this.licenseReader = reader;
            this.descriptor = descriptor;
            this.fallbackFactory = fallbackFactory;
        }

        @Override
        public Logger getLogger() {
            return licenseReader.getLogger();
        }

        /**
         * Gets a read-only collection of license limitations.
         *
         * @return A read-only collection of license limitations.
         */
        @Override
        public Collection<String> getLimitations() {
            return Lists.newArrayList(licenseReader.getLimitations(descriptor, fallbackFactory));
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

    protected final static class LicensingDescriptionServiceManager<L extends LicenseLimitations> extends SupportAdapterServiceManager<LicensingDescriptionService, AdapterLicensingDescriptorService> {
        private final Supplier<L> fallbackFactory;
        private final Class<L> descriptor;

        public LicensingDescriptionServiceManager(final Class<L> limitationsDescriptor,
                                                  final Supplier<L> fallbackFactory) {
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
                    fallbackFactory);
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
            identity.put(ADAPTER_NAME_IDENTITY_PROPERTY, getAdapterName());
            return createConfigurationDescriptionProvider(dependencies);
        }
    }

    /**
     * Represents name of the adapter.
     */
    public final String adapterName;

    /**
     * Initializes a new instance of the resource adapter activator.
     * @param adapterName The name of the adapter.
     * @param factory Resource adapter factory. Cannot be {@literal null}.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected ResourceAdapterActivator(final String adapterName,
                                       final ResourceAdapterFactory<TAdapter> factory,
                                       final SupportAdapterServiceManager<?, ?>... optionalServices){
        this(adapterName, factory, EMTPY_REQUIRED_SERVICES, optionalServices);
    }

    /**
     * Initializes a new instance of the resource adapter activator.
     * @param adapterName The name of the adapter.
     * @param factory Resource adapter factory. Cannot be {@literal null}.
     * @param adapterDependencies Adapter-level dependencies.
     * @param optionalServices Additional services exposed by adapter.
     */
    protected ResourceAdapterActivator(final String adapterName,
                                       final ResourceAdapterFactory<TAdapter> factory,
                                       final RequiredService<?>[] adapterDependencies,
                                       final SupportAdapterServiceManager<?, ?>[] optionalServices) {
        super(ArrayUtils.addToEnd(optionalServices, new ResourceAdapterRegistry<>(adapterName, factory, adapterDependencies), ProvidedService.class));
        this.adapterName = adapterName;
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
        activationProperties.publish(ADAPTER_NAME_HOLDER, adapterName);
        activationProperties.publish(LOGGER_HOLDER, getLogger());
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Activating resource adapters of type %s. Context: %s", adapterName,
                    LogicalOperation.current()));
        }
    }

    private BundleContext getAdapterContext(){
        return Utils.getBundleContextByObject(this);
    }


    /**
     * Gets logger associated with this activator.
     * @return A logger associated with this activator.
     */
    protected Logger getLogger(){
        return AbstractResourceAdapter.getLogger(adapterName);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    @Override
    protected final void deactivate(final ActivationPropertyReader activationProperties) throws Exception {
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.info(String.format("Unloading adapters of type %s. Context: %s",
                    adapterName,
                    LogicalOperation.current()));
        }
    }

    private OSGiLoggingContext getLoggingContext(){
        return OSGiLoggingContext.get(getLogger(), getAdapterContext());
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.itworks.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        try(final OSGiLoggingContext logger = getLoggingContext()) {
            logger.log(Level.SEVERE, String.format("Unable to activate %s resource adapter instance. Context: %s",
                    adapterName,
                    LogicalOperation.current()), e);
        }
    }

    /**
     * Handles an exception thrown by {@link } method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        try(final OSGiLoggingContext logger = getLoggingContext()){
            logger.log(Level.SEVERE, String.format("Unable to deactivate %s resource adapter instance. Context: %s",
                    adapterName,
                    LogicalOperation.current()), e);
        }
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
