package com.itworks.snamp.core;

import com.itworks.snamp.internal.semantics.MethodStub;
import org.osgi.framework.*;

import java.util.*;

import static com.itworks.snamp.internal.ReflectionUtils.getBundleContextByObject;

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
    public static abstract class ProvidedService<S extends FrameworkService, T extends S> implements AllServiceListener {
        /**
         * Represents service contract.
         */
        public final Class<S> serviceContract;
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
            try{
                if(registration != null) registration.unregister();
                registration = null;
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
     * Determines whether the service of the specified contract is published by this bundle.
     * @param serviceType Service contract descriptor.
     * @return {@literal true}, if the specified service of the specified contract is published
     *          by this bundle.
     */
    @SuppressWarnings("UnusedDeclaration")
    public final boolean isServiceExposed(final Class<? extends FrameworkService> serviceType){
        for(final ProvidedService<?, ?> providedService: providedServices)
            if(Objects.equals(serviceType, providedService.serviceContract)) return true;
        return false;
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
        for(final ProvidedService<?, ?> service: providedServices)
            service.unregister(context);
        providedServices.clear();
        deactivate(activationProperties);
    }

    /**
     * Stops the library.
     *
     * @param context The execution context of the library being stopped.
     */
    @Override
    @MethodStub
    protected final void shutdown(final BundleContext context) {
    }
}
