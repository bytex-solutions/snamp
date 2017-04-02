package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.concurrent.LazyStrongReference;
import com.google.common.collect.ObjectArrays;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an activator for SNAMP-specific bundle which exposes a set of services.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractServiceLibrary extends AbstractBundleActivator {
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

        private DynamicServiceLoggingScope(final ServiceListener requester,
                                           final String operationName){
            super(requester, operationName);
        }

        private static DynamicServiceLoggingScope update(final ServiceListener requester){
            return new DynamicServiceLoggingScope(requester, "updateDynamicService");
        }

        private static DynamicServiceLoggingScope delete(final ServiceListener requester){
            return new DynamicServiceLoggingScope(requester, "deleteDynamicService");
        }
    }

    /**
     * Represents state of the service publication.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    public enum ProvidedServiceState{

        /**
         * Service is not published.
         */
        NOT_PUBLISHED,

        /**
         * Service is published.
         */
        PUBLISHED
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
        private final Class<? super S>[] serviceContracts;
        protected final DependencyManager dependencies;

        private volatile ServiceRegistrationHolder<S, T> registration;
        private volatile ActivationPropertyReader properties;

        //fields as a part of tuple because it has the same lifecycle in this instance
        private WeakServiceListener dependencyTracker;   //weak reference to avoid leak of service listener inside of OSGi container
        private BundleContext activationContext;

        private ProvidedService(final Supplier<Class<? super S>[]> contracts, final RequiredService<?>... dependencies) {
            serviceContracts = contracts.get();
            this.dependencies = new DependencyManager(dependencies);
            registration = null;
            properties = EMPTY_ACTIVATION_PROPERTY_READER;
        }

        /**
         * Initializes a new holder for the provided service.
         * @param contract Contract of the provided service. Cannot be {@literal null}.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected ProvidedService(final Class<S> contract, final RequiredService<?>... dependencies) {
            this(() -> new Class[]{contract}, dependencies);
        }

        @SafeVarargs
        protected ProvidedService(final Class<S> mainContract, final RequiredService<?>[] dependencies, final Class<? super S>... subContracts){
            this(() -> ObjectArrays.concat(mainContract, subContracts), dependencies);
        }

        /**
         * Gets main service contract.
         * @return Main service contract.
         * @since 2.0
         */
        protected final Class<? super S> getServiceContract(){
            return ArrayUtils.getFirst(serviceContracts).orElseThrow(AssertionError::new);
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
            if(registration != null &&
                    Objects.equals(registration.getReference(), event.getServiceReference())) return;
            int resolvedDependencies = 0;
            for(final RequiredService<?> dependency: dependencies){
                dependency.processServiceEvent(context, event.getServiceReference(), event.getType());
                if(dependency.isResolved()) resolvedDependencies += 1;
            }
            switch (getState()){
                case PUBLISHED:
                    //dependency lost but service is activated
                    if(resolvedDependencies != dependencies.size()) {
                        final ProvidedServiceLoggingScope logger = ProvidedServiceLoggingScope.unregister(this);
                        try {
                            if (registration != null) {
                                final T serviceInstance = registration.get();
                                registration.unregister();
                                cleanupService(serviceInstance, false);
                            }
                        } catch (final Exception e) {
                            logger.unableToCleanupService(getServiceContract(), e);
                        } finally {
                            registration = null;
                            logger.close();
                        }
                    }
                    return;
                case NOT_PUBLISHED:
                    if(resolvedDependencies == dependencies.size()) {
                        final ProvidedServiceLoggingScope logger = ProvidedServiceLoggingScope.expose(this);
                        try {
                            activateAndRegisterService(context);
                        } catch (final Exception e) {
                            logger.unableToActivateService(getServiceContract(), e);
                            if (registration != null) registration.unregister();
                            registration = null;
                        } finally {
                            logger.close();
                        }
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
         * @return The state of this service provider.[
         */
        public final ProvidedServiceState getState() {
            final ServiceRegistrationHolder<S, T> registration = this.registration;
            return registration == null || registration.isUnregistered() ?
                    ProvidedServiceState.NOT_PUBLISHED :
                    ProvidedServiceState.PUBLISHED;
        }

        private synchronized void activateAndRegisterService(final BundleContext context) throws Exception {
            final Hashtable<String, Object> identity = new Hashtable<>(3);
            registration = new ServiceRegistrationHolder<>(serviceContracts,
                    activateService(identity),
                    identity,
                    context);
        }

        private synchronized void register(final BundleContext context, final ActivationPropertyReader properties) throws Exception {
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

        private synchronized void unregister(final BundleContext context) throws Exception {
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
                dependencies.unbind(context);
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
        protected abstract T activateService(final Map<String, Object> identity) throws Exception;
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
        private final LazyStrongReference<String> factoryPID;

        /**
         * Initializes a new holder for the provided service.
         *
         * @param dependencies A collection of service dependencies of dynamic services.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected DynamicServiceManager(final RequiredService<?>... dependencies) {
            super(ManagedServiceFactory.class, dependencies);
            factoryPID = new LazyStrongReference<>();
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
         * @param service The service to be updated.
         * @param configuration A new configuration of the service.
         * @return An updated service.
         * @throws Exception Unable to create new service or update the existing service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract TService updateService(final TService service,
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
        protected abstract void dispose(final TService service, final boolean bundleStop) throws Exception;

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary)} failed.
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
         * Logs error details when {@link #dispose(Object, boolean)} failed.
         * @param logger Logger used to write information about error.
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e An exception occurred when disposing service.
         */
        protected void failedToCleanupService(final Logger logger,
                                              final String servicePID,
                                              final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to deactivate service with PID %s", servicePID), e);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @return A new instance of the service.
         */
        @Override
        protected final ManagedServiceFactoryImpl<TService> activateService(final Map<String, Object> identity) throws Exception {
            final String factoryPID = getCachedFactoryPID();
            identity.put(Constants.SERVICE_PID, factoryPID);
            return new ManagedServiceFactoryImpl<TService>(){
                private static final long serialVersionUID = -7596205835324011065L;

                @Override
                public String getName() {
                    return factoryPID;
                }

                @Override
                public synchronized void updated(final String pid, final Dictionary<String, ?> properties) throws ConfigurationException {
                    TService service;
                    final LoggingScope logger = DynamicServiceLoggingScope.update(DynamicServiceManager.this);
                    try {
                        service = containsKey(pid) ?
                                updateService(get(pid), properties) :
                                activateService(pid, properties);
                    } catch (final ConfigurationException e) {
                        throw e;
                    } catch (final Exception e) {
                        service = null;
                        failedToUpdateService(logger, pid, properties, e);
                    }
                    finally {
                        logger.close();
                    }
                    if (service == null)
                        remove(pid);
                    else
                        put(pid, service);
                }

                @Override
                public synchronized void deleted(final String pid) {
                    final LoggingScope logger = DynamicServiceLoggingScope.delete(DynamicServiceManager.this);
                    try {
                        if (containsKey(pid))
                            dispose(remove(pid), false);
                    } catch (final Exception e) {
                        failedToCleanupService(logger, pid, e);
                    }
                }
            };
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
                        dispose(service, stopBundle);
                } finally {
                    si.clear();
                }
            });
        }
    }

    private static final class ServiceRegistrationHolder<S, T extends S> implements ServiceRegistration<S>, Supplier<T> {
        private final ServiceRegistration<?> registration;
        private T serviceInstance;

        ServiceRegistrationHolder(final Class<? super S>[] serviceContracts,
                                  final T service,
                                  final Dictionary<String, ?> identity,
                                  final BundleContext context) {
            serviceInstance = Objects.requireNonNull(service);
            final String[] serviceContractNames = Arrays.stream(serviceContracts).map(Class::getName).toArray(String[]::new);
            registration = context.registerService(serviceContractNames, service, identity);
        }

        @SuppressWarnings("unchecked")
        ServiceRegistrationHolder(final Class<S> serviceContract,
                                  final T service,
                                  final Dictionary<String, ?> identity,
                                  final BundleContext context){
            this(new Class[]{serviceContract}, service, identity, context);
        }

        @Override
        public void setProperties(final Dictionary<String, ?> properties) {
            registration.setProperties(properties);
        }

        Hashtable<String, ?> dumpProperties(){
            final String[] propertyNames = registration.getReference().getPropertyKeys();
            final Hashtable<String, Object> result = new Hashtable<>(propertyNames.length * 2);
            for(final String propertyName: propertyNames)
                result.put(propertyName, registration.getReference().getProperty(propertyName));
            return result;
        }

        @Override
        public T get() {
            return serviceInstance;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ServiceReference<S> getReference(){
            return (ServiceReference<S>) registration.getReference();
        }

        @Override
        public void unregister(){
            try {
                registration.unregister();
            }
            finally {
                serviceInstance = null;
            }
        }

        @Override
        public String toString() {
            return registration.toString();
        }

        boolean isUnregistered() {
            return registration == null;
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
        /**
         * Represents the contract of the dynamic service.
         */
        protected final Class<S> serviceContract;

        /**
         * Initializes a new dynamic service manager.
         * @param serviceContract The contract of the dynamic services.
         * @param dependencies A collection of dependencies required for the newly created service.
         */
        protected ServiceSubRegistryManager(final Class<S> serviceContract,
                                            final RequiredService<?>... dependencies){
            super(dependencies);
            this.serviceContract = serviceContract;
        }

        /**
         * Updates the service with a new configuration.
         * @param service The service to update.
         * @param configuration A new configuration of the service.
         * @return The updated service.
         * @throws Exception Unable to update service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract T update(final T service,
                                    final Dictionary<String, ?> configuration) throws Exception;

        /**
         * Automatically invokes by SNAMP when the dynamic service should be updated with
         * a new configuration.
         *
         * @param registration The service to be updated.
         * @param configuration        A new configuration of the service.
         * @return An updated service.
         * @throws Exception                                  Unable to create new service or update the existing service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @Override
        protected final ServiceRegistrationHolder<S, T> updateService(ServiceRegistrationHolder<S, T> registration,
                                                                      final Dictionary<String, ?> configuration) throws Exception {
            final T oldService = registration.get();
            final T newService = update(oldService, configuration);
            if (newService == null) {
                dispose(registration);
                registration = null;
            }
            else if (oldService != newService) {
                //save the identity of the service and removes registration of the previous version of service
                final Hashtable<String, ?> identity = dispose(registration);
                registration = new ServiceRegistrationHolder<>(serviceContract, newService, identity, super.getBundleContext());
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
        protected abstract T createService(final Map<String, Object> identity,
                                           final Dictionary<String, ?> configuration) throws Exception;

        /**
         * Automatically invokes by SNAMP when the new dynamic service should be created.
         *
         * @param servicePID    The persistent identifier associated with a newly created service.
         * @param configuration A new configuration of the service.
         * @return A new instance of the service.
         * @throws Exception                                  Unable to instantiate the service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @Override
        protected final ServiceRegistrationHolder<S, T> activateService(final String servicePID,
                                                                        final Dictionary<String, ?> configuration) throws Exception {
            final Hashtable<String, Object> identity = new Hashtable<>(4);
            identity.put(Constants.SERVICE_PID, servicePID);
            final T service = createService(identity, configuration);
            return service != null ? new ServiceRegistrationHolder<>(serviceContract, service, identity, super.getBundleContext()) : null;
        }

        /**
         * Releases all resources associated with the service instance.
         * @param service A service to dispose.
         * @param identity Service identity.
         * @throws Exception Unable to dispose service.
         */
        protected abstract void cleanupService(final T service,
                                               final Map<String, ?> identity) throws Exception;

        private Hashtable<String, ?> dispose(final ServiceRegistrationHolder<S, T> registration) throws Exception{
            final T serviceInstance = registration.get();
            final Hashtable<String, ?> properties = registration.dumpProperties();
            try {
                registration.unregister();
            }
            finally {
                cleanupService(serviceInstance, properties);
            }
            return properties;
        }

        /**
         * Automatically invokes by SNAMP when service is disposing.
         *
         * @throws Exception Unable to dispose the service.
         */
        @Override
        protected final void dispose(final ServiceRegistrationHolder<S, T> registration, final boolean bundleStop) throws Exception {
            dispose(registration);
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
