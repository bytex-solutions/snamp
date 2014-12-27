package com.itworks.snamp.core;

import com.itworks.snamp.internal.annotations.MethodStub;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.*;
import java.util.logging.Level;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Represents an activator for SNAMP-specific bundle which exposes a set of services.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractServiceLibrary extends AbstractBundleActivator {

    /**
     * Represents state of the service publication.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static enum ProvidedServiceState{
        /**
         * Service is not published.
         */
        NOT_PUBLISHED,

        /**
         * Service publishing is in progress.
         */
        PUBLISHING,

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
    public static abstract class ProvidedService<S, T extends S> implements AllServiceListener {
        /**
         * Represents service contract.
         */
        protected final Class<S> serviceContract;
        private final List<RequiredService<?>> ownDependencies;

        private ServiceRegistration<S> registration;
        private T serviceInstance;
        private ActivationPropertyReader properties;

        /**
         * Initializes a new holder for the provided service.
         * @param contract Contract of the provided service. Cannot be {@literal null}.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected ProvidedService(final Class<S> contract, final RequiredService<?>... dependencies){
            if(contract == null) throw new IllegalArgumentException("contract is null.");
            serviceContract = contract;
            ownDependencies = Arrays.asList(dependencies);
            registration = null;
            serviceInstance = null;
            properties = emptyActivationPropertyReader;
        }

        /**
         * Gets a value of the bundle activation property.
         * @param propertyDef The definition of the property.
         * @param <V> Type of the property value.
         * @return The value of the property.
         */
        protected final <V> V getActivationPropertyValue(final ActivationProperty<V> propertyDef){
            return properties.getValue(propertyDef);
        }

        private synchronized void serviceChanged(final BundleContext context, final ServiceEvent event) {
            //avoid cyclic reference tracking
            if(getState() == ProvidedServiceState.PUBLISHING || registration != null &&
                    Objects.equals(registration.getReference(), event.getServiceReference())) return;
            int resolvedDependencies = 0;
            for(final RequiredService<?> dependency: ownDependencies){
                dependency.processServiceEvent(context, event.getServiceReference(), event.getType());
                if(dependency.isResolved()) resolvedDependencies += 1;
            }
            switch (getState()){
                case PUBLISHED:
                    //dependency lost but service is activated
                    if(resolvedDependencies != ownDependencies.size())
                        try{
                            registration.unregister();
                            cleanupService(serviceInstance, false);
                        }
                        catch (final Exception e){
                            //ignores this exception
                        }
                        finally {
                            serviceInstance = null;
                            registration = null;
                        }
                return;
                case NOT_PUBLISHED:
                    if(resolvedDependencies == ownDependencies.size())
                        try {
                            activateAndRegisterService(context);
                        }
                        catch (final Exception e) {
                            if(registration != null) registration.unregister();
                            serviceInstance = null;
                            registration = null;
                            throw new ServiceException(String.format("Unable to activate %s service", serviceContract),
                                    ServiceException.FACTORY_EXCEPTION, e);
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
            serviceChanged(getBundleContextByObject(this), event);
        }

        /**
         * Gets state of this service provider.
         * @return The state of this service provider.[
         */
        public final ProvidedServiceState getState(){
            if(serviceInstance != null) return registration == null ? ProvidedServiceState.PUBLISHING : ProvidedServiceState.PUBLISHED;
            else return ProvidedServiceState.NOT_PUBLISHED;
        }

        private void activateAndRegisterService(final BundleContext context) throws Exception{
            final Hashtable<String, Object> identity = new Hashtable<>(3);
            this.serviceInstance = activateService(identity, ownDependencies.toArray(new RequiredService<?>[ownDependencies.size()]));
            this.registration = context.registerService(serviceContract, serviceInstance, identity);
        }

        private void register(final BundleContext context, final ActivationPropertyReader properties) throws Exception{
            this.properties = properties;
            if(ownDependencies.isEmpty()) //instantiate and register service now because there are no dependencies
                activateAndRegisterService(context);
            else {
                for (final RequiredService<?> dependency : ownDependencies)
                    for (final ServiceReference<?> serviceRef : dependency.getCandidates(context))
                        serviceChanged(context, new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
                //dependency tracking required
                context.addServiceListener(this);
            }
        }

        private void unregister(final BundleContext context) throws Exception{
            if(!ownDependencies.isEmpty()) context.removeServiceListener(this);
            //cancels registration
            try{
                if(registration != null) registration.unregister();
            }
            catch (final IllegalStateException ignored){
                //unregister can throws this exception and it must be suppressed
            }
            finally {
                this.registration = null;
            }
            //release service instance
            try{
                if(serviceInstance != null) cleanupService(serviceInstance, true);
            }
            finally {
                this.serviceInstance = null;
                //releases all dependencies
                for(final RequiredService<?> dependency: ownDependencies)
                    dependency.unbind(context);
                properties = emptyActivationPropertyReader;
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

        }

        /**
         * Creates a new instance of the service.
         * @param identity A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        protected abstract T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception;
    }

    private static abstract class ManagedServiceFactoryImpl<TService> extends HashMap<String, TService> implements ManagedServiceFactory{

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
        private final String factoryPID;

        /**
         * Initializes a new holder for the provided service.
         *
         * @param factoryPID The base persistent identifier used as a prefix for individual dynamic services configuration.
         * @param dependencies A collection of service dependencies of dynamic services.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected DynamicServiceManager(final String factoryPID, final RequiredService<?>... dependencies) {
            super(ManagedServiceFactory.class, dependencies);
            this.factoryPID = factoryPID;
        }

        /**
         * Automatically invokes by SNAMP when the dynamic service should be updated with
         * a new configuration.
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
         * @param servicePID The persistent identifier associated with a newly created service.
         * @param configuration A new configuration of the service.
         * @param dependencies A collection of dependencies required for the newly created service.
         * @return A new instance of the service.
         * @throws Exception Unable to instantiate the service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract TService activateService(final String servicePID,
                                                    final Dictionary<String, ?> configuration,
                                                    final RequiredService<?>... dependencies) throws Exception;

        /**
         * Automatically invokes by SNAMP when service is disposing.
         * @param service A service to dispose.
         * @throws Exception Unable to dispose the service.
         */
        protected abstract void dispose(final TService service, final boolean bundleStop) throws Exception;

        /**
         * Logs error details when {@link #activateService(String, java.util.Dictionary, com.itworks.snamp.core.AbstractBundleActivator.RequiredService[])} failed.
         * @param servicePID The persistent identifier associated with a newly created service.
         * @param configuration The configuration of the service.
         * @param e An exception occurred when instantiating service.
         */
        protected void failedToActivateService(final String servicePID,
                                                        final Dictionary<String, ?> configuration,
                                                        final Exception e){
            try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to activate service with PID %s and %s configuration", servicePID, configuration), e);
            }
        }

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary)} failed.
         * @param servicePID The persistent identifier associated with the service.
         * @param configuration The configuration of the service.
         * @param e An exception occurred when updating service.
         */
        protected void failedToUpdateService(final String servicePID,
                                             final Dictionary<String, ?> configuration,
                                             final Exception e){
            try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to update service with PID %s and %s configuration", servicePID, configuration), e);
            }
        }

        /**
         * Logs error details when {@link #dispose(Object, boolean)} failed.
         * @param servicePID The persistent identifier of the service to dispose.
         * @param e An exception occurred when disposing service.
         */
        protected void failedToCleanupService(final String servicePID,
                                              final Exception e){
            try(final OsgiLoggingContext logger = getLoggingContext()){
                logger.log(Level.SEVERE, String.format("Unable to deactivate service with PID %s", servicePID), e);
            }
        }

        private OsgiLoggingContext getLoggingContext(){
            return OsgiLoggingContext.getLogger(factoryPID, getBundleContextByObject(this));
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected final ManagedServiceFactoryImpl<TService> activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(Constants.SERVICE_PID, factoryPID);
            return new ManagedServiceFactoryImpl<TService>(){
                @Override
                public String getName() {
                    return factoryPID;
                }

                @Override
                public synchronized void updated(final String pid, final Dictionary<String, ?> properties) throws ConfigurationException {
                    if(containsKey(pid))
                        try{
                            put(pid, updateService(get(pid), properties));
                        }
                        catch (final ConfigurationException e){
                            throw e;
                        }
                        catch (final Exception e){
                            failedToUpdateService(pid, properties, e);
                        }
                    else try{
                        put(pid, activateService(pid, properties, dependencies));
                    }
                    catch (final ConfigurationException e){
                        throw e;
                    }
                    catch (final Exception e){
                        failedToActivateService(pid, properties, e);
                    }
                }

                @Override
                public void deleted(final String pid) {
                    if(containsKey(pid))
                        try{
                            dispose(remove(pid), false);
                        }
                        catch (final Exception e){
                            failedToCleanupService(pid, e);
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
            try {
                for (final TService service : serviceInstance.values())
                    dispose(service, stopBundle);
            }
            finally {
                serviceInstance.clear();
            }
        }
    }

    private static final class ServiceRegistrationHolder<S, T extends S>{
        private final ServiceRegistration<S> registration;
        private final T serviceImpl;

        private ServiceRegistrationHolder(final Class<S> serviceContract,
                                          final T service,
                                          final Dictionary<String, ?> identity,
                                          final BundleContext context){
            registration = context.registerService(serviceContract, service, identity);
            serviceImpl = service;
        }

        private void unregister(){
            registration.unregister();
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
    public static abstract class ServiceSubRegistryManager<S, T extends S> extends DynamicServiceManager<ServiceRegistrationHolder<S, T>> {
        /**
         * Represents the contract of the dynamic service.
         */
        protected final Class<S> serviceContract;

        /**
         * Initializes a new dynamic service manager.
         * @param serviceContract The contract of the dynamic services.
         * @param factoryPID The base persistent identifier used as a prefix for individual dynamic services configuration.
         * @param dependencies A collection of dependencies required for the newly created service.
         */
        protected ServiceSubRegistryManager(final Class<S> serviceContract,
                                            final String factoryPID,
                                            final RequiredService<?>... dependencies){
            super(factoryPID, dependencies);
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
        protected abstract T update(final T service, final Dictionary<String, ?> configuration) throws Exception;

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
        protected final ServiceRegistrationHolder<S, T> updateService(ServiceRegistrationHolder<S, T> registration, final Dictionary<String, ?> configuration) throws Exception {
            final T oldService = registration.serviceImpl;
            final T newService = update(oldService, configuration);
            if(oldService != newService) {
                //save the identity of the service
                final ServiceReference<S> ref = registration.registration.getReference();
                final Hashtable<String, Object> identity = new Hashtable<>(ref.getPropertyKeys().length);
                for(final String key: registration.registration.getReference().getPropertyKeys())
                    identity.put(key, ref.getProperty(key));
                //re-register updated service
                dispose(registration, false);
                registration = new ServiceRegistrationHolder<>(serviceContract, newService, identity, getBundleContextByObject(this));
            }
            return registration;
        }

        /**
         * Creates a new service.
         * @param identity The registration properties to fill.
         * @param configuration A new configuration of the service.
         * @param dependencies The dependencies required for the service.
         * @return A new instance of the service.
         * @throws Exception Unable to instantiate a new service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid configuration exception.
         */
        protected abstract T createService(final Map<String, Object> identity,
                                           final Dictionary<String, ?> configuration,
                                           final RequiredService<?>... dependencies) throws Exception;

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
        protected final ServiceRegistrationHolder<S, T> activateService(final String servicePID,
                                                               final Dictionary<String, ?> configuration,
                                                               final RequiredService<?>... dependencies) throws Exception {
            final Hashtable<String, Object> identity = new Hashtable<>(10);
            identity.put(Constants.SERVICE_PID, servicePID);
            return new ServiceRegistrationHolder<>(serviceContract,
                    createService(identity, configuration, dependencies),
                    identity,
                    getBundleContextByObject(this));
        }

        /**
         * Releases all resources associated with the service instance.
         * @param service A service to dispose.
         * @throws Exception Unable to dispose service.
         */
        protected abstract void cleanupService(final T service) throws Exception;

        /**
         * Automatically invokes by SNAMP when service is disposing.
         *
         * @throws Exception Unable to dispose the service.
         */
        @Override
        protected void dispose(final ServiceRegistrationHolder<S, T> registration, final boolean bundleStop) throws Exception {
            registration.unregister();
            cleanupService(registration.serviceImpl);
        }
    }

    /**
     * Represents a holder of a collection of provided services.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ProvidedServices{
        /**
         * Exposes all provided services via the input collection.
         * @param services A collection of provided services to fill.
         * @param activationProperties A collection of activation properties to read.
         * @param bundleLevelDependencies A collection of resolved bundle-level dependencies.
         */
        void provide(final Collection<ProvidedService<?, ?>> services,
                     final ActivationPropertyReader activationProperties,
                     final RequiredService<?>... bundleLevelDependencies);
    }

    private static final class ListOfProvidedServices extends ArrayList<ProvidedService<?, ?>> implements ProvidedServices{

        public ListOfProvidedServices(final ProvidedService<?, ?>... services){
            super(Arrays.asList(services));
        }

        @Override
        public void provide(final Collection<ProvidedService<?, ?>> services,
                            final ActivationPropertyReader activationProperties,
                            final RequiredService<?>... bundleLevelDependencies) {
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
        this.providedServices = new ArrayList<>(10);
    }

    /**
     * Gets an instance of the provided service.
     * @param providerType The type of the service provider.
     * @param <S> The contract of the provided service.
     * @return Strongly typed reference to the provided service.
     */
    protected final <S> S getProvidedService(final Class<? extends ProvidedService<S, ? extends S>> providerType){
        for(final ProvidedService<?, ?> provider: providedServices)
            if(providerType.isInstance(provider))
                return ((ProvidedService<S, ? extends S>)providerType.cast(provider)).serviceInstance;
        return null;
    }

    /**
     * Starts the service library.
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     * @throws Exception Unable to start service library.
     */
    protected abstract void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception;

    /**
     * Starts the bundle.
     *
     * @param context                 The execution context of the library being started.
     * @param bundleLevelDependencies A collection of library-level dependencies to fill.
     * @throws Exception The bundle cannot be started.
     */
    @Override
    protected final void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        start(bundleLevelDependencies);
    }

    /**
     * Activates this service library.
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    protected abstract void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception;

    /**
     * Registers all services in this library.
     * @param context The execution context of the library being activated.
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies A collection of resolved dependencies.
     * @throws Exception Bundle activation error.
     */
    @Override
    protected final void activate(final BundleContext context,
                                  final ActivationPropertyPublisher activationProperties,
                                  final RequiredService<?>... dependencies) throws Exception {
        activate(activationProperties, dependencies);
        serviceRegistry.provide(providedServices, getActivationProperties(), dependencies);
        for(final ProvidedService<?, ?> service: providedServices)
            service.register(context, getActivationProperties());
    }

    /**
     * Deactivates this library.
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    protected abstract void deactivate(final ActivationPropertyReader activationProperties) throws Exception;

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
    protected final void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        unregister(context, providedServices);
        deactivate(activationProperties);
    }

    /**
     * Stops the library.
     *
     * @param context The execution context of the library being stopped.
     * @throws java.lang.Exception Abnormal library termination.
     */
    @Override
    protected final void shutdown(final BundleContext context) throws Exception{
        if(getState() == ActivationState.ACTIVATED)
            deactivate(context, getActivationProperties());
    }
}
