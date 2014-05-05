package com.itworks.snamp.core;

import com.itworks.snamp.internal.MethodStub;
import org.apache.commons.collections4.Closure;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import java.util.*;

import static com.itworks.snamp.internal.ReflectionUtils.*;

/**
 * Represents a base class for all SNAMP-specific bundle activators.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractBundleActivator implements BundleActivator {

    /**
     * Represents dependency descriptor.
     * @param <S> Type of the required service.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class RequiredService<S> {
        private final Class<S> dependencyContract;
        private ServiceReference<?> reference;

        /**
         * Initializes a new dependency descriptor.
         * @param dependencyType Contract of the required service. Cannot be {@literal null}.
         * @throws IllegalArgumentException dependencyType is {@literal null}.
         */
        protected RequiredService(final Class<S> dependencyType){
            if(dependencyType == null) throw new IllegalArgumentException("dependencyType is null.");
            this.dependencyContract = dependencyType;
            this.reference = null;
        }

        /**
         * Provides matching reference to the conditions of this dependency.
         * <p>This method should be implemented in stateless manner.</p>
         * @param reference The service reference to check.
         * @return {@literal true}, if the specified reference matches to the dependency resolving conditions;
         *          otherwise, {@literal false}.
         */
        protected abstract boolean match(final ServiceReference<?> reference);

        /**
         * Informs this dependency about resolving dependency.
         * @param serviceInstance An instance of the resolved service.
         * @param properties Service properties.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        protected abstract void bind(final S serviceInstance,
                                     final Dictionary<String, ?> properties,
                                     final Map<String, ?> processingContext);

        /**
         * Informs this dependency about detaching dependency.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        protected abstract void unbind(final Map<String, ?> processingContext);

        /**
         * Informs this dependency about modification of the service properties.
         * <p>
         *     In the default implementation this method does nothing.
         * </p>
         * @param properties A new properties of the service.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        @SuppressWarnings("UnusedParameters")
        @MethodStub
        protected void update(final Dictionary<String, ?> properties, final Map<String, ?> processingContext){

        }

        /**
         * Determines whether the dependency is resolved.
         * @return {@literal true}, if this dependency is resolved and reference to the service
         * is caught; otherwise, {@literal false}.
         */
        public final boolean isResolved(){
            return reference != null;
        }

        private boolean bind(final BundleContext context, final ServiceReference<?> reference, final Map<String, ?> processingContext){
            if(!isResolved() && match(reference) && isInstanceOf(reference, dependencyContract))
                    try {
                        bind(dependencyContract.cast(context.getService(reference)), getProperties(reference), processingContext);
                        return true;
                    }
                    finally {
                        this.reference = reference;
                    }
            return false;
        }

        private boolean unbind(final BundleContext context,
                               final ServiceReference<?> reference,
                               final Map<String, ?> processingContext){
            if(isResolved() && match(reference))
                try {
                    unbind(processingContext);
                    return true;
                }
                finally {
                    context.ungetService(reference);
                    this.reference = null;
                }
            else return false;
        }

        private boolean unbind(final BundleContext context, final Map<String, ?> processingContext){
            return unbind(context, this.reference, processingContext);
        }

        private boolean update(final ServiceReference<?> reference, final Map<String, ?> processingContext) {
            if(isResolved() && match(reference)){
                update(getProperties(reference), processingContext);
                return true;
            }
            return false;
        }

        protected final void handleService(final ServiceReference<?> reference, final int eventType, final Map<String, ?> handleContext){
            handleService(getBundleContextByObject(this), reference, eventType, handleContext);
        }

        protected final void handleService(final ServiceReference<?> reference, final int eventType){
            handleService(reference, eventType, Collections.<String, Object>emptyMap());
        }

        private synchronized void handleService(final BundleContext context,
                                   final ServiceReference<?> reference,
                                   final int eventType,
                                   Map<String, ?> sharedContext) {
            sharedContext = Collections.unmodifiableMap(sharedContext);
            switch (eventType){
                case ServiceEvent.REGISTERED:
                    bind(context, reference, sharedContext);
                return;
                case ServiceEvent.UNREGISTERING:
                case ServiceEvent.MODIFIED_ENDMATCH:
                    unbind(context, reference, sharedContext);
                return;
                case ServiceEvent.MODIFIED:
                    update(reference, sharedContext);
            }
        }
    }

    /**
     * Represents bundle-level dependency that is not bounded to the specified provided service.
     * @param <S> Type of the required service.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected abstract static class BundleLevelDependency<S> extends RequiredService<S> implements AllServiceListener{

        /**
         * Initializes a new dependency descriptor.
         *
         * @param dependencyType Contract of the required service. Cannot be {@literal null}.
         * @throws IllegalArgumentException
         *          dependencyType is {@literal null}.
         */
        protected BundleLevelDependency(final Class<S> dependencyType) {
            super(dependencyType);
        }

        /**
         * Receives notification that a service has had a lifecycle change.
         *
         * @param event The {@code ServiceEvent} object.
         */
        @Override
        public final void serviceChanged(final ServiceEvent event) {
            handleService(event.getServiceReference(), event.getType());
        }
    }

    /**
     * Provides an accessor for the required service.
     * @param <S> Contract of the required service.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static abstract class RequiredServiceAccessor<S> extends RequiredService<S>{
        private S serviceInstance;

        /**
         * Initializes a new dependency descriptor.
         *
         * @param dependencyType Contract of the required service. Cannot be {@literal null}.
         * @throws IllegalArgumentException
         *          dependencyType is {@literal null}.
         */
        protected RequiredServiceAccessor(final Class<S> dependencyType) {
            super(dependencyType);
        }

        /**
         * Gets a required service if this dependency is in resolved state.
         * @return A required service if this dependency is in resolved state.
         * @see #isResolved()
         */
        public final S getService(){
            return serviceInstance;
        }

        /**
         * Informs this dependency about resolving dependency.
         *
         * @param serviceInstance An instance of the resolved service.
         * @param properties      Service properties.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        @Override
        protected void bind(final S serviceInstance,
                            final Dictionary<String, ?> properties,
                            final Map<String, ?> processingContext) {
            this.serviceInstance = serviceInstance;
        }

        /**
         * Informs this dependency about detaching dependency.
         * @param processingContext A map that comes from the provided service that owns this dependency.
         */
        @Override
        protected void unbind(final Map<String, ?> processingContext) {
            serviceInstance = null;
        }
    }

    /**
     * Represents simple dependency that catches any service with matched contract.
     * This class cannot be inherited.
     * @param <S> Type of the requested service contract.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class SimpleDependency<S> extends RequiredServiceAccessor<S>{
        /**
         * Initializes a new simple dependency descriptor.
         * @param serviceContract Type of the requested service. Cannot be {@literal null}.
         */
        public SimpleDependency(final Class<S> serviceContract){
            super(serviceContract);
        }

        /**
         * Provides matching reference to the conditions of this dependency.
         * <p>This method should be implemented in stateless manner.</p>
         *
         * @param reference The service reference to check.
         * @return {@literal true}, if the specified reference matches to the dependency resolving conditions;
         * otherwise, {@literal false}.
         */
        @Override
        protected boolean match(final ServiceReference<?> reference) {
            return true;
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
    public static abstract class ProvidedService<S extends FrameworkService, T extends S> implements AllServiceListener {
        /**
         * Represents service contract.
         */
        public final Class<S> serviceContract;
        private final List<RequiredService<?>> ownDependencies;
        private ServiceRegistration<S> registration;
        private T serviceInstance;
        /**
         * Service activation context that comes from {@link AbstractBundleActivator#init(java.util.Map, com.itworks.snamp.core.AbstractBundleActivator.ServiceRegistryProcessor, java.util.Collection)} method.
         */
        private final Map<String, Object> sharedContext;

        /**
         * Initializes a new holder for the provided service.
         * @param contract Contract of the provided service. Cannot be {@literal null}.
         * @param dependencies A collection of service dependencies.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected ProvidedService(final Class<S> contract, final RequiredService<?>... dependencies){
            if(contract == null) throw new IllegalArgumentException("contract is null.");
            this.serviceContract = contract;
            this.ownDependencies = Arrays.asList(dependencies);
            this.serviceInstance = null;
            this.registration = null;
            this.sharedContext = new HashMap<>();
        }

        /**
         * Gets shared context that comes from {@link #init(java.util.Map, com.itworks.snamp.core.AbstractBundleActivator.ServiceRegistryProcessor, java.util.Collection)} method.
         * @return The bundle shared context.
         */
        protected final Map<String, ?> getSharedContext(){
            return sharedContext;
        }

        private synchronized void serviceChanged(final BundleContext context, final ServiceEvent event) {
            //avoid cyclic reference tracking
            if(ownDependencies.isEmpty() || registration != null &&
                    Objects.equals(registration.getReference(), event.getServiceReference())) return;
            int resolvedDependencies = 0;
            for(final RequiredService<?> dependency: ownDependencies){
                dependency.handleService(context, event.getServiceReference(), event.getType(), sharedContext);
                if(dependency.isResolved()) resolvedDependencies += 1;
            }
            //determines whether all dependencies are resolved
            if(resolvedDependencies == ownDependencies.size() && !isPublished())
                try {
                    activateAndRegisterService(context);
                }
                catch (final Exception e) {
                    throw new ServiceException(String.format("Unable to activate %s service", serviceContract),
                            ServiceException.FACTORY_EXCEPTION, e);
                }
            else if(isPublished()){ //dependency lost, forces cleanup
                registration.unregister();
                registration = null;
                try{
                    cleanupService(serviceInstance, false);
                }
                catch (final Exception e){
                    //ignores this exception
                }
                finally {
                    serviceInstance = null;
                }
            }
        }

        /**
         * Recompute all dependencies.
         *
         * @param event The {@code ServiceEvent} object.
         */
        @Override
        public final void serviceChanged(final ServiceEvent event) {
            serviceChanged(getBundleContextByObject(this), event);
        }

        /**
         * Determines whether this service is published and accessible to other bundles.
         * @return {@literal true}, if this service is published and accessible to other bundles;
         *          otherwise, {@literal false}.
         */
        public final boolean isPublished(){
            return registration != null && serviceInstance != null;
        }

        private void activateAndRegisterService(final BundleContext context) throws Exception{
            final Hashtable<String, Object> identity = new Hashtable<>(3);
            this.serviceInstance = activateService(identity, ownDependencies.toArray(new RequiredService[ownDependencies.size()]));
            this.registration = context.registerService(serviceContract, serviceInstance, identity);
        }

        private void register(final BundleContext context, final Map<String, Object> sharedState) throws Exception{
            this.sharedContext.putAll(sharedState);
            if(ownDependencies.isEmpty()) //instantiate and register service now because there are no dependencies
                activateAndRegisterService(context);
            else for(final RequiredService<?> dependency: ownDependencies) {
                ServiceReference<?>[] refs = context.getAllServiceReferences(dependency.dependencyContract.getName(), null);
                if(refs == null) refs = new ServiceReference<?>[0];
                for (final ServiceReference<?> serviceRef : refs)
                    serviceChanged(context, new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
            }
            //dependency tracking required
            context.addServiceListener(this);
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
                    dependency.unbind(context, sharedContext);
                this.sharedContext.clear();
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
         * @param sharedContext Shared context.
         */
        void provide(final Collection<ProvidedService<?, ?>> services, final Map<String, ?> sharedContext);
    }

    private static final class ListOfProvidedServices extends ArrayList<ProvidedService<?, ?>> implements ProvidedServices{

        public ListOfProvidedServices(final ProvidedService<?, ?>... services){
            super(Arrays.asList(services));
        }

        /**
         * Exposes all provided services via the input collection.
         *
         * @param services      A collection of provided services to fill.
         * @param sharedContext Shared context.
         */
        @Override
        public void provide(final Collection<ProvidedService<?, ?>> services, final Map<String, ?> sharedContext) {
            services.addAll(this);
        }
    }

    /**
     * Provides access to the OSGi service registry at bundle's initialization phase.
     * <p>
     *     You should not implement this interface directly in your code.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static interface ServiceRegistryProcessor{
        /**
         * Queries and processes the specified service obtained from OSGi service registry.
         * @param serviceType Requested service contract descriptor.
         * @param processor An object that handles the resolved service.
         * @param <S> Type of the requested service contract.
         */
        <S> boolean processService(final Class<S> serviceType, final Closure<S> processor);
    }

    private final ProvidedServices serviceRegistry;
    private final Map<String, Object> sharedContext;
    private final List<ProvidedService<?, ?>> providedServices;
    private final List<BundleLevelDependency<?>> bundleDependencies;

    /**
     * Initializes a new bundle with the specified collection of provided services.
     * @param providedServices A collection of provided services.
     */
    protected AbstractBundleActivator(final ProvidedService<?, ?>... providedServices){
        this(new ListOfProvidedServices(providedServices));
    }

    /**
     * Initializes a new bundle with the specified registry of provided services.
     * @param providedServices A factory that exposes a collection of provided services. Cannot be {@literal null}.
     * @throws java.lang.IllegalArgumentException providedServices is {@literal null}.
     */
    protected AbstractBundleActivator(final ProvidedServices providedServices){
        if(providedServices == null) throw new IllegalArgumentException("providedServices is null.");
        this.serviceRegistry = providedServices;
        this.providedServices = new ArrayList<>(10);
        this.sharedContext = new HashMap<>(10);
        this.bundleDependencies = new ArrayList<>(5);
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
     * Initializes a bundle and fills the map that will be shared between provided services.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param sharedContext The activation context to initialize.
     * @param serviceReg An object that provides access to the OSGi service registry.
     * @param bundleLevelDependencies A collection of bundle-level dependencies.
     * @throws java.lang.Exception Initialization failed.
     */
    @MethodStub
    protected void init(final Map<String, Object> sharedContext,
                        final ServiceRegistryProcessor serviceReg,
                        final Collection<BundleLevelDependency<?>> bundleLevelDependencies) throws Exception{

    }

    private static ServiceRegistryProcessor createRegistryProcessor(final BundleContext context){
        return new ServiceRegistryProcessor() {
            @Override
            public <S> boolean processService(final Class<S> serviceType, final Closure<S> processor) {
                final ServiceReference<S> ref = context.getServiceReference(serviceType);
                if(ref == null) return false;
                try{
                    processor.execute(context.getService(ref));
                }
                finally {
                    context.ungetService(ref);
                }
                return true;
            }
        };
    }

    /**
     * Starts the SNAMP-specific bundle.
     * <p>
     *     This method acquires reference to {@link LogService} OSGi service.
     * </p>
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this bundle is
     *         marked as stopped and the Framework will remove this bundle's
     *         listeners, unregister all services registered by this bundle, and
     *         release all services used by this bundle.
     */
    public final void start(final BundleContext context) throws Exception {
        init(sharedContext, createRegistryProcessor(context), bundleDependencies);
        //init bundle-level dependencies
        for(final ServiceListener listener: bundleDependencies)
            context.addServiceListener(listener);
        serviceRegistry.provide(providedServices, sharedContext);
        //register provided service
        for(final ProvidedService<?, ?> service: providedServices)
            service.register(context, sharedContext);
    }

    /**
     * Stops the SNAMP-specific bundle.
     * <p>
     *     This method releases reference to {@link LogService} OSGi service.
     * </p>
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the bundle is still
     *                   marked as stopped, and the Framework will remove the bundle's
     *                   listeners, unregister all services registered by the bundle, and
     *                   release all services used by the bundle.
     */
    public final void stop(final BundleContext context) throws Exception{
        for(final ProvidedService<?, ?> providedService: providedServices)
            providedService.unregister(context);
        //disable bundle-level dependencies
        for(final ServiceListener listener: bundleDependencies)
            context.removeServiceListener(listener);
        sharedContext.clear();
        providedServices.clear();
    }

    /**
     * Finds dependency by its type and required service contract.
     * @param descriptor The dependency descriptor.
     * @param serviceContract The service contract required by dependency.
     * @param dependencies A collection of dependencies.
     * @param <S> Type of the service contract.
     * @param <D> Type of the dependency.
     * @return Search result; or {@literal null} if dependency not found.
     */
    public static <S, D extends RequiredService<S>> D findDependency(final Class<D> descriptor, final Class<S> serviceContract, final RequiredService<?>... dependencies){
        for(final RequiredService<?> dependency: dependencies)
            if(descriptor.isInstance(dependency) && Objects.equals(dependency.dependencyContract, serviceContract))
                return descriptor.cast(dependency);
        return null;
    }

    /**
     * Finds dependency by its required service contract.
     * @param serviceContract The service contract required by dependency.
     * @param dependencies A collection of dependencies.
     * @param <S> Type of the service contract.
     * @return Search result; or {@literal null} if dependency not found.
     */
    @SuppressWarnings({"UnusedDeclaration", "unchecked"})
    public static <S> RequiredService<S> findDependency(final Class<S> serviceContract, final RequiredService<?>... dependencies){
        return findDependency(RequiredService.class, serviceContract, dependencies);
    }

    /**
     * Obtains a service from the collection of dependencies.
     * @param descriptor The dependency type that provides direct access to the requested service.
     * @param serviceContract The service contract required by dependency.
     * @param dependencies A collection of dependencies.
     * @param <S> Type of the service contract.
     * @param <D> Type of the dependency.
     * @return The resolved service; or {@literal null} if it is not available.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static <S, D extends RequiredServiceAccessor<S>> S getDependency(final Class<D> descriptor, final Class<S> serviceContract, final RequiredService<?>... dependencies){
        final D found = findDependency(descriptor, serviceContract, dependencies);
        return found != null ? found.getService() : null;
    }

    /**
     * Gets properties of the service that is represented by the specified reference.
     * @param reference The reference to the service.
     * @return A dictionary that provides access to the service properties.
     */
    public static Dictionary<String, ?> getProperties(final ServiceReference<?> reference){
        return reference != null ? new Dictionary<String, Object>() {
            @Override
            public int size() {
                return reference.getPropertyKeys().length;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public Enumeration<String> keys() {
                return Collections.enumeration(Arrays.asList(reference.getPropertyKeys()));
            }

            @Override
            public Enumeration<Object> elements() {
                final String[] properties = reference.getPropertyKeys();
                final List<Object> values = new ArrayList<>(properties.length);
                for(final String p: properties)
                    values.add(reference.getProperty(p));
                return Collections.enumeration(values);
            }

            public Object get(final String key){
                return reference.getProperty(key);
            }

            @Override
            public Object get(final Object key) {
                return key instanceof String ? get((String)key) : null;
            }

            @Override
            public Object put(final String key, final Object value) {
                throw new UnsupportedOperationException("This dictionary is read-only.");
            }

            @Override
            public Object remove(final Object key) {
                throw new UnsupportedOperationException("This dictionary is read-only.");
            }
        } : null;
    }
}
