package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.WeakEventListener;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an activator for SNAMP-specific bundle which exposes a set of services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractServiceLibrary extends AbstractBundleActivator {
    /**
     * Write-only service identity builder.
     */
    @FunctionalInterface
    public interface ServiceIdentityBuilder extends BiConsumer<String, Object>, Constants {
        default void setServletContext(final String urlContext){
            accept("alias", urlContext);
        }

        default void setServicePID(final String servicePID){
            accept(SERVICE_PID, servicePID);
        }

        default void acceptAll(final Map<String, ?> identity){
            identity.forEach(this);
        }
    }

    private static final class ProvidedServiceLoggingScope extends LoggingScope {

        private ProvidedServiceLoggingScope(final ServiceListener requester,
                                            final String operationName){
            super(requester, operationName);
        }

        private void unableToCleanupService(final Class<?> serviceContract, final Exception e) {
            log(Level.SEVERE, String.format("Unable to cleanup service %s", serviceContract), e);
        }

        private void unableToActivateService(final Class<?> serviceContract, final  Exception e) {
            log(Level.SEVERE, String.format("Unable to activate %s service", serviceContract), e);
        }

        private static ProvidedServiceLoggingScope expose(final ServiceListener requester){
            return new ProvidedServiceLoggingScope(requester, "exposeOsgiService");
        }

        private static ProvidedServiceLoggingScope unregister(final ServiceListener requester){
            return new ProvidedServiceLoggingScope(requester, "unregisterOsgiService");
        }
    }

    private static final class DynamicServiceLoggingScope extends LoggingScope {

        private DynamicServiceLoggingScope(final Logger logger,
                                           final Class<?> requester,
                                           final String operationName){
            super(logger, requester, operationName);
        }

        static DynamicServiceLoggingScope update(final Logger logger, final Class<?> requester){
            return new DynamicServiceLoggingScope(logger, requester, "updateDynamicService");
        }

        static DynamicServiceLoggingScope delete(final Logger logger, final Class<?> requester){
            return new DynamicServiceLoggingScope(logger, requester, "deleteDynamicService");
        }
    }

    private static final class WeakServiceListener extends WeakEventListener<ServiceListener, ServiceEvent> implements ServiceListener {
        WeakServiceListener(@Nonnull final ServiceListener listener){
            super(listener);
        }

        @Override
        protected void invoke(@Nonnull final ServiceListener listener, @Nonnull final ServiceEvent event) {
            listener.serviceChanged(event);
        }

        @Override
        public void serviceChanged(final ServiceEvent event) {
            invoke(event);
        }
    }

    /**
     * Represents a holder for the provided service.
     * <p>
     *     The derived class must have parameterless constructor.
     * </p>
     * @param <S> Contract of the provided service.
     * @param <T> Implementation of the provided service.
     */
    public static abstract class ProvidedService<S, T extends S> implements ServiceListener {
        protected final DependencyManager dependencies;
        private final Set<Class<? super T>> contract;

        private volatile ServiceRegistrationHolder<S, T> registration;
        private volatile ActivationPropertyReader properties;

        //fields as a part of tuple because it has the same lifecycle in this instance
        private WeakServiceListener dependencyTracker;   //weak reference to avoid leak of service listener inside of OSGi container
        private BundleContext activationContext;

        /**
         * Initializes a new holder for the provided service.
         * @param contract Contract of the provided service. Cannot be {@literal null}.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected ProvidedService(@Nonnull final Class<S> contract, final RequiredService<?>... dependencies) {
            this(contract, dependencies, ArrayUtils.emptyArray(Class.class));
        }

        @SafeVarargs
        protected ProvidedService(@Nonnull final Class<S> mainContract, final RequiredService<?>[] dependencies, final Class<? super T>... subContracts){
            this.contract = ImmutableSet.<Class<? super T>>builder().add(mainContract).add(subContracts).build();
            this.dependencies = new DependencyManager(dependencies).freeze();
            registration = null;
            properties = EMPTY_ACTIVATION_PROPERTY_READER;
        }

        private Class<? super T> getServiceContract(){
            return contract.stream().findFirst().orElseThrow(AssertionError::new);
        }

        /**
         * Gets a value of the bundle activation property.
         * @param propertyDef The definition of the property.
         * @param <V> Type of the property value.
         * @return The value of the property.
         */
        protected final <V> V getActivationPropertyValue(final ActivationProperty<V> propertyDef){
            return properties.getProperty(propertyDef);
        }

        private synchronized void serviceChanged(final BundleContext context, final ServiceEvent event) {
            //avoid cyclic reference tracking
            if (registration != null &&
                    Objects.equals(registration.getReference(), event.getServiceReference())) return;
            int resolvedDependencies = 0;
            for (final RequiredService<?> dependency : dependencies)
                if (dependency.processServiceEvent(event.getServiceReference(), event.getType()))
                    resolvedDependencies += 1;
            if (registration == null) {   //not published
                if (resolvedDependencies == dependencies.size()) {
                    final ProvidedServiceLoggingScope logger = ProvidedServiceLoggingScope.expose(this);
                    try {
                        activateAndRegisterService(context);
                    } catch (final Exception e) {
                        logger.unableToActivateService(getServiceContract(), e);
                    } finally {
                        logger.close();
                    }
                }
            } else if (resolvedDependencies != dependencies.size()) {
                final ProvidedServiceLoggingScope logger = ProvidedServiceLoggingScope.unregister(this);
                try {
                    final T serviceInstance = registration.get();
                    registration.close();
                    cleanupService(serviceInstance, false);
                } catch (final Exception e) {
                    logger.unableToCleanupService(getServiceContract(), e);
                } finally {
                    registration = null;
                    logger.close();
                }
            }
        }

        /**
         * Receives notification that a service has had a lifecycle change.
         *
         * @param event The {@code ServiceEvent} object.
         */
        @Override
        public final void serviceChanged(final ServiceEvent event) {
            serviceChanged(activationContext, event);
        }

        /**
         * Gets bundle context used in {@link #serviceChanged(ServiceEvent)}.
         * @return Bundle context used in {@link #serviceChanged(ServiceEvent)}.
         */
        final BundleContext getBundleContext() {
            return activationContext;
        }

        /**
         * Gets state of this service provider.
         * @return The state of this service provider.
         */
        public final boolean isPublished() {
            final ServiceRegistrationHolder<?, ?> registration = this.registration;
            return registration != null && registration.isPublished();
        }

        private synchronized void activateAndRegisterService(final BundleContext context) throws Exception {
            final Hashtable<String, Object> identity = new Hashtable<>(3);
            final T serviceInstance = activateService(identity::put);
            //registration happens after instantiation of the service
            registration = new ServiceRegistrationHolder<>(context, serviceInstance, identity, contract);
        }

        synchronized void register(final BundleContext context, final ActivationPropertyReader properties) throws Exception {
            this.properties = properties;
            activationContext = context;
            if (dependencies.isEmpty()) //instantiate and register service now because there are no dependencies, no dependency tracking is required
                activateAndRegisterService(context);
            else {
                final DependencyListeningFilterBuilder filter = new DependencyListeningFilterBuilder();
                for (final RequiredService<?> dependency : dependencies) {
                    filter.append(dependency);
                    for (final ServiceReference<?> serviceRef : dependency.getCandidates(context))
                        serviceChanged(context, new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
                }
                //dependency tracking required
                filter.applyServiceListener(context, dependencyTracker = new WeakServiceListener(this));
            }
        }

        synchronized void unregister(final BundleContext context) throws Exception {
            if (dependencyTracker != null) {
                context.removeServiceListener(dependencyTracker);
                dependencyTracker.clear();     //help GC
                dependencyTracker = null;
            }
            //cancels registration
            final T serviceInstance;
            if (registration != null) {
                serviceInstance = registration.get();
                try {
                    registration.unregister();
                } catch (final IllegalStateException ignored) {
                    //unregister can throws this exception and it must be suppressed
                } finally {
                    registration = null;
                }
            } else
                serviceInstance = null;

            //release service instance
            try {
                if (serviceInstance != null)
                    cleanupService(serviceInstance, true);
            } finally {
                //releases all dependencies
                dependencies.unbind();
                properties = EMPTY_ACTIVATION_PROPERTY_READER;
                activationContext = null;
            }
        }

        /**
         * Provides service cleanup operations.
         * <p>
         *     In the default implementation this method does nothing.
         * </p>
         * @param serviceInstance An instance of the hosted service to cleanup.
         * @param stopBundle {@literal true}, if this method calls when the owner bundle is stopping;
         *                   {@literal false}, if this method calls when loosing dependency.
         */
        @MethodStub
        protected void cleanupService(final T serviceInstance, final boolean stopBundle) throws Exception{
            if(serviceInstance instanceof AutoCloseable)
                ((AutoCloseable)serviceInstance).close();
        }

        /**
         * Creates a new instance of the service.
         * @param identity A dictionary of properties that uniquely identifies service instance.
         * @return A new instance of the service.
         */
        @Nonnull
        protected abstract T activateService(final ServiceIdentityBuilder identity) throws Exception;
    }

    private static abstract class ManagedServiceFactoryImpl<TService> extends HashMap<String, TService> implements ManagedServiceFactory{
        private static final long serialVersionUID = 6353271076932722292L;

        private synchronized <E extends Throwable> void synchronizedInvoke(final Acceptor<? super ManagedServiceFactoryImpl<TService>, E> action) throws E{
            action.accept(this);
        }
    }

    /**
     * Describes a holder for a dynamic set of services managed with {@link org.osgi.service.cm.ManagedServiceFactory} service.
     * <p>
     *     Each service activated through the factory is just a custom object and it is not required
     *     to register it in the OSGi Service Registry.
     * </p>
     * @param <TService> Type of the dynamic service.
     */
    public static abstract class DynamicServiceManager<TService> extends ProvidedService<ManagedServiceFactory, ManagedServiceFactoryImpl<TService>>{
        private final LazyReference<String> factoryPID;

        /**
         * Initializes a new holder for the provided service.
         *
         * @param dependencies A collection of service dependencies of dynamic services.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected DynamicServiceManager(final RequiredService<?>... dependencies) {
            super(ManagedServiceFactory.class, dependencies);
            factoryPID = LazyReference.strong();
        }

        private String getCachedFactoryPID() {
            return factoryPID.lazyGet(this, DynamicServiceManager::getFactoryPID);
        }

        /**
         * Gets the base persistent identifier used as a prefix for individual dynamic services configuration.
         * @return The base persistent identifier. Cannot be {@literal null} or empty string.
         */
        protected abstract String getFactoryPID();

        /**
         * Automatically invokes by SNAMP when the dynamic service should be updated with
         * a new configuration.
         * <p>
         *     Don't worry about synchronization of this method because
         *     SNAMP infrastructure will call this method in synchronized context.
         * @param servicePID The persistent identifier associated with a newly created service.
         * @param service The service to be updated.
         * @param configuration A new configuration of the service.
         * @return An updated service.
         * @throws Exception Unable to create new service or update the existing service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract TService updateService(final String servicePID,
                                                  final TService service,
                                                  final Dictionary<String, ?> configuration) throws Exception;

        /**
         * Automatically invokes by SNAMP when the new dynamic service should be created.
         * <p>
         *     Don't worry about synchronization of this method because
         *     SNAMP infrastructure will call this method in synchronized context.
         * @param servicePID The persistent identifier associated with a newly created service.
         * @param configuration A new configuration of the service.
         * @return A new instance of the service.
         * @throws Exception Unable to instantiate the service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract TService activateService(final String servicePID,
                                                    final Dictionary<String, ?> configuration) throws Exception;

        /**
         * Automatically invokes by SNAMP when service is disposing.
         * <p>
         *     Don't worry about synchronization of this method because
         *     SNAMP infrastructure will call this method in synchronized context.
         * @param service A service to dispose.
         * @throws Exception Unable to dispose the service.
         */
        protected abstract void disposeService(final TService service, final boolean bundleStop) throws Exception;

        /**
         * Log error details when {@link #updateService(String, Object, java.util.Dictionary)} failed.
         * @param logger Logger used to write information about error.
         * @param servicePID The persistent identifier associated with the service.
         * @param configuration The configuration of the service.
         * @param e An exception occurred when updating service.
         */
        protected void failedToUpdateService(final Logger logger,
                                             final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e) {
            logger.log(Level.SEVERE,
                    String.format("Unable to update service with PID %s and %s configuration", servicePID, configuration),
                    e);
        }

        /**
         * Logs error details when {@link #disposeService(Object, boolean)} failed.
         * @param logger Logger used to write information about error.
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e An exception occurred when disposing service.
         */
        protected void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            getLogger().log(Level.SEVERE, String.format("Unable to deactivate service with PID %s", servicePID), e);
        }

        protected Logger getLogger(){
            return LoggerProvider.getLoggerForObject(this);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @return A new instance of the service.
         */
        @Override
        @Nonnull
        protected final ManagedServiceFactoryImpl<TService> activateService(final ServiceIdentityBuilder identity) throws Exception {
            final String factoryPID = getCachedFactoryPID();
            identity.setServicePID(factoryPID);
            return new ManagedServiceFactoryImpl<TService>(){
                private static final long serialVersionUID = -7596205835324011065L;

                @Override
                public String getName() {
                    return factoryPID;
                }

                @Override
                public synchronized void updated(final String pid, final Dictionary<String, ?> properties) throws ConfigurationException {
                    TService service;
                    final LoggingScope logger = DynamicServiceLoggingScope.update(getLogger(), getClass());
                    try {
                        service = containsKey(pid) ?
                                updateService(pid, get(pid), properties) :
                                activateService(pid, properties);
                    } catch (final ConfigurationException e) {
                        throw e;
                    } catch (final Exception e) {
                        service = null;
                        failedToUpdateService(logger, pid, properties, e);
                    } finally {
                        logger.close();
                    }
                    if (service == null)
                        remove(pid);
                    else
                        put(pid, service);
                }

                @Override
                public synchronized void deleted(final String pid) {
                    final LoggingScope logger = DynamicServiceLoggingScope.delete(getLogger(), getClass());
                    try {
                        if (containsKey(pid))
                            disposeService(remove(pid), false);
                    } catch (final Exception e) {
                        failedToCleanupService(logger, pid, e);
                    } finally {
                        logger.close();
                    }
                }
            };
        }

        protected void destroyed(){

        }

        /**
         * Provides service cleanup operations.
         * <p>
         * In the default implementation this method does nothing.
         * </p>
         *
         * @param serviceInstance An instance of the hosted service to cleanup.
         * @param stopBundle      {@literal true}, if this method calls when the owner bundle is stopping;
         *                        {@literal false}, if this method calls when loosing dependency.
         */
        @Override
        protected final void cleanupService(final ManagedServiceFactoryImpl<TService> serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.synchronizedInvoke((Map<?, TService> si) -> {
                try {
                    for (final TService service : si.values())
                        disposeService(service, stopBundle);
                } finally {
                    si.clear();
                }
            });
            destroyed();
        }
    }

    /**
     * Represents a registry of dynamic OSGi services that are managed by {@link org.osgi.service.cm.ManagedServiceFactory} service.
     * <p>
     *     This class is an entry point for writing and managing a set of services with identical contract
     *     that depends on the dynamic configuration stored using {@link org.osgi.service.cm.ConfigurationAdmin} service.
     * </p>
     * @param <S> The contract of the dynamic service.
     * @param <T> The implementation of the dynamic service.
     */
    @ThreadSafe
    public static abstract class ServiceSubRegistryManager<S, T extends S> extends DynamicServiceManager<ServiceRegistrationHolder<S, T>> {
        private final Set<Class<? super T>> contract;

        /**
         * Initializes a new dynamic service manager.
         * @param serviceContract The contract of the dynamic services.
         * @param dependencies A collection of dependencies required for the newly created service.
         */
        protected ServiceSubRegistryManager(final Class<S> serviceContract,
                                            final RequiredService<?>... dependencies){
            this(serviceContract, dependencies, ArrayUtils.emptyArray(Class.class));
        }

        @SafeVarargs
        protected ServiceSubRegistryManager(final Class<S> serviceContract,
                                            final RequiredService<?>[] dependencies,
                                            final Class<? super T>... subContracts){
            super(dependencies);
            contract = ImmutableSet.<Class<? super T>>builder().add(serviceContract).add(subContracts).build();
        }

        private ServiceRegistrationHolder<S, T> createServiceRegistration(final T newService, final Dictionary<String, ?> identity) {
            return new ServiceRegistrationHolder<>(getBundleContext(), newService, identity, contract);
        }

        /**
         * Updates the service with a new configuration.
         * @param service The service to update.
         * @param configuration A new configuration of the service.
         * @return The updated service.
         * @throws Exception Unable to update service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract T updateService(final T service,
                                           final Dictionary<String, ?> configuration) throws Exception;

        @Override
        protected final ServiceRegistrationHolder<S, T> updateService(final String servicePID,
                                                                      ServiceRegistrationHolder<S, T> registration,
                                                                      final Dictionary<String, ?> configuration) throws Exception {
            final T oldService = registration.get();
            final T newService = updateService(oldService, configuration);
            if (newService == null) {
                disposeService(registration);
                registration = null;
            } else if (oldService != newService) {
                //destroy previous service and instantiate new one
                disposeService(registration);
                registration = activateService(servicePID, configuration);
            }
            return registration;
        }

        /**
         * Creates a new service.
         * @param identity The registration properties to fill.
         * @param configuration A new configuration of the service.
         * @return A new instance of the service.
         * @throws Exception Unable to instantiate a new service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid configuration exception.
         */
        protected abstract T activateService(final ServiceIdentityBuilder identity,
                                             final Dictionary<String, ?> configuration) throws Exception;

        @Override
        protected final ServiceRegistrationHolder<S, T> activateService(final String servicePID,
                                                                        final Dictionary<String, ?> configuration) throws Exception {
            final Hashtable<String, Object> identity = new Hashtable<>(4);
            identity.put(Constants.SERVICE_PID, servicePID);
            final T service = activateService(identity::put, configuration);
            return service != null ? createServiceRegistration(service, identity) : null;
        }

        protected abstract void disposeService(final T service,
                                               final Map<String, ?> identity) throws Exception;

        private void disposeService(final ServiceRegistrationHolder<S, T> registration) throws Exception {
            final T serviceInstance = registration.get();
            assert serviceInstance != null;
            final Hashtable<String, ?> properties = registration.dumpProperties();
            Utils.closeAll(registration, () -> disposeService(serviceInstance, properties));
        }

        /**
         * Automatically invokes by SNAMP when service is disposing.
         *
         * @throws Exception Unable to dispose the service.
         */
        @Override
        protected final void disposeService(final ServiceRegistrationHolder<S, T> registration, final boolean bundleStop) throws Exception {
            disposeService(registration);
        }
    }

    /**
     * Represents a holder of a collection of provided services.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    protected interface ProvidedServices{
        /**
         * Exposes all provided services via the input collection.
         * @param services A collection of provided services to fill.
         * @param activationProperties A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of resolved bundle-level dependencies.
         */
        void provide(final Collection<ProvidedService<?, ?>> services,
                     final ActivationPropertyReader activationProperties,
                     final DependencyManager bundleLevelDependencies);
    }

    private static final class ListOfProvidedServices extends ArrayList<ProvidedService<?, ?>> implements ProvidedServices{
        private static final long serialVersionUID = 341418339520679799L;

        private ListOfProvidedServices(final ProvidedService<?, ?>... services){
            super(Arrays.asList(services));
        }

        @Override
        public void provide(final Collection<ProvidedService<?, ?>> services,
                            final ActivationPropertyReader activationProperties,
                            final DependencyManager bundleLevelDependencies) {
            services.addAll(this);
        }
    }

    private final ProvidedServices serviceRegistry;
    private final List<ProvidedService<?, ?>> providedServices;

    /**
     * Initializes a new bundle with the specified collection of provided services.
     * @param providedServices A collection of provided services.
     */
    protected AbstractServiceLibrary(final ProvidedService<?, ?>... providedServices){
        this(new ListOfProvidedServices(providedServices));
    }

    /**
     * Initializes a new bundle with the specified registry of provided services.
     * @param providedServices A factory that exposes a collection of provided services. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException providedServices is {@literal null}.
     */
    protected AbstractServiceLibrary(final ProvidedServices providedServices){
        if(providedServices == null) throw new IllegalArgumentException("providedServices is null.");
        this.serviceRegistry = providedServices;
        this.providedServices = new LinkedList<>();
    }

    /**
     * Registers all services in this library.
     * @param context The execution context of the library being activated.
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies Dependencies resolved by this activator.
     * @throws Exception Bundle activation error.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void activate(final BundleContext context,
                                  final ActivationPropertyPublisher activationProperties,
                                               final DependencyManager dependencies) throws Exception {
        serviceRegistry.provide(providedServices, getActivationProperties(), dependencies);
        for (final ProvidedService<?, ?> service : providedServices)
            service.register(context, getActivationProperties());
    }

    private static void unregister(final BundleContext context, final Collection<ProvidedService<?, ?>> providedServices) throws Exception {
        for (final ProvidedService<?, ?> service : providedServices)
            service.unregister(context);
        providedServices.clear();
    }

    /**
     * Deactivates this library.
     * <p>
     * This method will be invoked when at least one dependency was lost.
     * </p>
     *
     * @param context The execution context of the library being deactivated.
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Deactivation error.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        unregister(context, providedServices);
    }

    /**
     * Stops the library.
     *
     * @param context The execution context of the library being stopped.
     * @throws java.lang.Exception Abnormal library termination.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void shutdown(final BundleContext context) throws Exception {
        for(final ProvidedService<?, ?> providedService: providedServices)
            providedService.unregister(context);
    }
}
