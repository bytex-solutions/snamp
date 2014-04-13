package com.itworks.snamp.core;

import com.itworks.snamp.internal.MethodStub;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;

import java.util.*;

import static com.itworks.snamp.internal.ReflectionUtils.getBundleContextByObject;
import static com.itworks.snamp.internal.ReflectionUtils.isInstanceOf;

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
    protected static abstract class RequiredService<S> {
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
         */
        protected abstract void bind(final S serviceInstance, final Dictionary<String, ?> properties);

        /**
         * Informs this dependency about detaching dependency.
         */
        protected abstract void unbind();

        /**
         * Informs this dependency about modification of the service properties.
         * <p>
         *     In the default implementation this method does nothing.
         * </p>
         * @param properties A new properties of the service.
         */
        @MethodStub
        protected void update(final Dictionary<String, ?> properties){

        }

        /**
         * Determines whether the dependency is resolved.
         * @return
         */
        public final boolean isResolved(){
            return reference != null;
        }

        private boolean bind(final BundleContext context, final ServiceReference<?> reference){
            if(!isResolved() && match(reference) && isInstanceOf(reference, dependencyContract))
                    try {
                        bind(dependencyContract.cast(context.getService(reference)), getProperties(reference));
                        return true;
                    }
                    finally {
                        this.reference = reference;
                    }
            return false;
        }

        private boolean unbind(final BundleContext context, final ServiceReference<?> reference){
            if(isResolved() && match(reference))
                try {
                    unbind();
                    return true;
                }
                finally {
                    context.ungetService(reference);
                    this.reference = null;
                }
            else return false;
        }

        private boolean unbind(final BundleContext context){
            return unbind(context, this.reference);
        }

        private boolean update(final ServiceReference<?> reference) {
            if(isResolved() && match(reference)){
                update(getProperties(reference));
                return true;
            }
            return false;
        }

        protected final void handleService(final ServiceReference<?> reference, final int eventType){
            handleService(getBundleContextByObject(this), reference, eventType);
        }

        private void handleService(final BundleContext context, final ServiceReference<?> reference, final int eventType) {
            switch (eventType){
                case ServiceEvent.REGISTERED:
                    bind(context, reference);
                case ServiceEvent.UNREGISTERING:
                case ServiceEvent.MODIFIED_ENDMATCH:
                    unbind(context, reference);
                case ServiceEvent.MODIFIED:
                    update(reference);
            }
        }
    }

    /**
     * Represents required service that can be shared between several provided services.
     * @param <S> Type of the required service.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected abstract static class SharedDependency<S> extends RequiredService<S> implements ServiceListener{

        /**
         * Initializes a new dependency descriptor.
         *
         * @param dependencyType Contract of the required service. Cannot be {@literal null}.
         * @throws IllegalArgumentException
         *          dependencyType is {@literal null}.
         */
        protected SharedDependency(final Class<S> dependencyType) {
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
    protected static abstract class RequiredServiceAccessor<S> extends RequiredService<S>{
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
         */
        @Override
        protected void bind(final S serviceInstance, final Dictionary<String, ?> properties) {
            this.serviceInstance = serviceInstance;
        }

        /**
         * Informs this dependency about detaching dependency.
         */
        @Override
        protected final void unbind() {
            serviceInstance = null;
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
    protected static abstract class ProvidedService<S extends FrameworkService, T extends S> implements ServiceListener {
        /**
         * Represents service contract.
         */
        public final Class<S> serviceContract;
        private final List<RequiredService<?>> dependencies;
        private ServiceRegistration<S> registration;
        private T serviceInstance;
        /**
         * Service activation context that comes from {@link AbstractBundleActivator#init(java.util.Map)} method.
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
            this.dependencies = Arrays.asList(dependencies);
            this.serviceInstance = null;
            this.registration = null;
            this.sharedContext = new HashMap<>();
        }

        /**
         * Gets property from the shared context that comes from {@link #init(java.util.Map)} method.
         * @param propertyName The name of the property.
         * @param propertyType The type of the property.
         * @param <T> Type of the property value.
         * @return The property value from the shared context; or, {@literal null} if property doesn' exist.
         */
        protected final <T> T getSharedContextProperty(final String propertyName, final Class<T> propertyType){
            if(sharedContext.containsKey(propertyName)){
                final Object result = sharedContext.get(propertyName);
                return propertyType.isInstance(result) ? propertyType.cast(result) : null;
            }
            else return null;
        }

        /**
         * Recompute all dependencies.
         *
         * @param event The {@code ServiceEvent} object.
         */
        @Override
        public final void serviceChanged(final ServiceEvent event) {
            final BundleContext context = getBundleContextByObject(this);
            int resolvedDependencies = 0;
            for(final RequiredService<?> dependency: dependencies){
                dependency.handleService(context, event.getServiceReference(), event.getType());
                if(dependency.isResolved()) resolvedDependencies += 1;
            }
            //determines whether all dependencies are resolved
            if(resolvedDependencies == dependencies.size()){
                serviceInstance = activateService(dependencies.toArray(new RequiredService[dependencies.size()]));
                registration = context.registerService(serviceContract, serviceInstance, serviceInstance.getIdentity());
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
         * Determines whether this service is published and accessible to other bundles.
         * @return {@literal true}, if this service is published and accessible to other bundles;
         *          otherwise, {@literal false}.
         */
        public final boolean isPublished(){
            return registration != null && serviceInstance != null;
        }

        private void register(final BundleContext context, final Map<String, Object> sharedState){
            this.sharedContext.putAll(sharedState);
            if(dependencies.isEmpty()){
                //instantiate and register service now because there is no dependencies
                this.serviceInstance = activateService();
                registration = context.registerService(serviceContract, serviceInstance, serviceInstance.getIdentity());
            }
            else  //dependency tracking required
                context.addServiceListener(this);
        }

        private void unregister(final BundleContext context) throws Exception{
            if(!dependencies.isEmpty()) context.removeServiceListener(this);
            if(registration != null) registration.unregister();
            registration = null;
            try{
                cleanupService(this.serviceInstance, true);
            }
            finally {
                this.serviceInstance = null;
                //releases all dependencies
                for(final RequiredService<?> dependency: dependencies)
                    dependency.unbind(context);
                dependencies.clear();
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
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        protected abstract T activateService(final RequiredService<?>... dependencies);
    }

    private final Collection<Class<? extends ProvidedService<?, ?>>> serviceFactories;
    private final Collection<ProvidedService<?, ?>> providedServices;
    private final Map<String, Object> sharedContext;

    /**
     * Initializes a new bundle with the specified collection of provided services.
     * @param providedServices A collection of provided services.
     */
    protected AbstractBundleActivator(final Class<? extends ProvidedService<?, ?>>... providedServices){
        this.serviceFactories = Arrays.asList(providedServices);
        this.providedServices = new ArrayList<>(providedServices.length);
        this.sharedContext = new HashMap<>(10);
    }

    /**
     * Determines whether the service of the specified contract is published by this bundle.
     * @param serviceType Service contract descriptor.
     * @return {@literal true}, if the specified service of the specified contract is published
     *          by this bundle.
     */
    public final boolean isServiceExposed(final Class<? extends FrameworkService> serviceType){
        for(final ProvidedService<?, ?> providedService: providedServices)
            if(Objects.equals(serviceType, providedService.serviceContract)) return true;
        return false;
    }

    /**
     * Gets a collection of shared dependencies stored in the shared context.
     * @return A collection of shared dependencies.
     */
    protected final Collection<SharedDependency<?>> getSharedDependencies(){
        final List<SharedDependency<?>> dependencies = new ArrayList<>(sharedContext.size());
        for(final Object val: sharedContext.values())
            if(val instanceof SharedDependency<?>)
                dependencies.add((SharedDependency<?>)val);
        return dependencies;
    }

    /**
     * Initializes a bundle and fills the map that will be shared between provided services.
     * <p>
     *     In the default implementation this method does nothing.
     * </p>
     * @param sharedContext The activation context to initialize.
     */
    @MethodStub
    protected void init(final Map<String, Object> sharedContext){

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
    @Override
    public final void start(final BundleContext context) throws Exception {
        init(sharedContext);
        //init shared dependencies
        for(final ServiceListener listener: getSharedDependencies())
            context.addServiceListener(listener);
        //register provided service
        for(final Class<? extends ProvidedService<?, ?>> factoryType: serviceFactories){
            final ProvidedService<?, ?> providedService = factoryType.newInstance();
            providedService.register(context, sharedContext);
        }
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
    @Override
    public final void stop(final BundleContext context) throws Exception{
        for(final ProvidedService<?, ?> providedService: providedServices)
            providedService.unregister(context);
        providedServices.clear();
        //disable shared dependencies
        for(final ServiceListener listener: getSharedDependencies())
            context.removeServiceListener(listener);
        sharedContext.clear();
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
