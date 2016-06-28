package com.bytex.snamp.core;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.ThreadSafe;
import com.bytex.snamp.concurrent.LazyContainers;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.internal.Utils;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.*;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents an activator for SNAMP-specific bundle which exposes a set of services.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class AbstractServiceLibrary extends AbstractBundleActivator {
    private static final class ProvidedServiceLogicalOperation extends RichLogicalOperation{
        private static final String SERVICE_CONTRACT_PARAMETER = "serviceContract";
        private static final String EXPORTING_BUNDLE_NAME_PARAMETER = "exportingBundleName";

        private ProvidedServiceLogicalOperation(final String loggerName,
                                                final String operationName,
                                                final Class<?> serviceContract,
                                                final BundleContext context){
            super(loggerName, operationName, ImmutableMap.of(SERVICE_CONTRACT_PARAMETER, serviceContract,
                    EXPORTING_BUNDLE_NAME_PARAMETER, context.getBundle().getSymbolicName()));
        }

        private static ProvidedServiceLogicalOperation expose(final String loggerName,
                                                              final Class<?> contract,
                                                              final BundleContext context){
            return new ProvidedServiceLogicalOperation(loggerName, "exposeOsgiService", contract, context);
        }

        private static ProvidedServiceLogicalOperation unregister(final String loggerName,
                                                                  final Class<?> contract,
                                                                  final BundleContext context){
            return new ProvidedServiceLogicalOperation(loggerName, "unregisterOsgiService", contract, context);
        }
    }

    private static final class DynamicServiceLogicalOperation extends RichLogicalOperation{
        private static final String SERVICE_PID_PARAMETER = "servicePID";

        private DynamicServiceLogicalOperation(final String loggerName,
                                               final String operationName,
                                               final String servicePID){
            super(loggerName, operationName, ImmutableMap.of(SERVICE_PID_PARAMETER, servicePID));
        }

        private static DynamicServiceLogicalOperation update(final String loggerName,
                                                             final String servicePID){
            return new DynamicServiceLogicalOperation(loggerName, "updateDynamicService", servicePID);
        }

        private static DynamicServiceLogicalOperation delete(final String loggerName,
                                                             final String servicePID){
            return new DynamicServiceLogicalOperation(loggerName, "deleteDynamicService", servicePID);
        }
    }

    private static final class SubRegistryLogicalOperation extends RichLogicalOperation{
        private static final String SERVICE_PID_PARAMETER = DynamicServiceLogicalOperation.SERVICE_PID_PARAMETER;
        private static final String SERVICE_CONTRACT_PARAMETER = ProvidedServiceLogicalOperation.SERVICE_CONTRACT_PARAMETER;

        private SubRegistryLogicalOperation(final String loggerName,
                                            final String operationName,
                                            final String servicePID,
                                            final Class<?> serviceContract){
            super(loggerName,
                    operationName,
                    ImmutableMap.of(SERVICE_PID_PARAMETER, servicePID,
                            SERVICE_CONTRACT_PARAMETER, serviceContract));
        }

        private static SubRegistryLogicalOperation update(final String loggerName,
                                                          final String servicePID,
                                                          final Class<?> contract){
            return new SubRegistryLogicalOperation(loggerName, "updateDynamicOsgiService", servicePID, contract);
        }

        private static SubRegistryLogicalOperation delete(final String loggerName,
                                                          final String servicePID,
                                                          final Class<?> contract){
            return new SubRegistryLogicalOperation(loggerName, "deleteDynamicOsgiService", servicePID, contract);
        }
    }

    /**
     * Represents state of the service publication.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
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
        /**
         * Represents service contract.
         */
        protected final Class<S> serviceContract;
        private final ImmutableList<RequiredService<?>> ownDependencies;

        private ServiceRegistrationHolder<S, T> registration;
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
            ownDependencies = ImmutableList.copyOf(dependencies);
            registration = null;
            properties = emptyActivationPropertyReader;
        }

        /**
         * Returns the bundle that declares this service.
         * @return The bundle that declares this service.
         */
        protected final Bundle getBundle(){
            return Utils.getBundleContextOfObject(this).getBundle();
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
            if(registration != null &&
                    Objects.equals(registration.getReference(), event.getServiceReference())) return;
            int resolvedDependencies = 0;
            for(final RequiredService<?> dependency: ownDependencies){
                dependency.processServiceEvent(context, event.getServiceReference(), event.getType());
                if(dependency.isResolved()) resolvedDependencies += 1;
            }
            switch (getState()){
                case PUBLISHED:
                    //dependency lost but service is activated
                    if(resolvedDependencies != ownDependencies.size()) {
                        final LogicalOperation logger = ProvidedServiceLogicalOperation.unregister(getClass().getName(),
                                serviceContract,
                                context);
                        try {
                            if (registration != null) {
                                final T serviceInstance = registration.get();
                                registration.unregister();
                                cleanupService(serviceInstance, false);
                            }
                        } catch (final Exception e) {
                            logger.log(Level.SEVERE, String.format("Unable to cleanup service %s", serviceContract), e);
                        } finally {
                            registration = null;
                            logger.close();
                        }
                    }
                    return;
                case NOT_PUBLISHED:
                    if(resolvedDependencies == ownDependencies.size()) {
                        final LogicalOperation logger = ProvidedServiceLogicalOperation.expose(getClass().getName(), serviceContract, context);
                        try {
                            activateAndRegisterService(context);
                        } catch (final Exception e) {
                            logger.log(Level.SEVERE, String.format("Unable to activate %s service", serviceContract), e);
                            if (registration != null) registration.unregister();
                            this.registration = null;
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
            serviceChanged(getBundleContextOfObject(this), event);
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

        private void activateAndRegisterService(final BundleContext context) throws Exception{
            final Hashtable<String, Object> identity = new Hashtable<>(3);
            this.registration = new ServiceRegistrationHolder<>(serviceContract,
                    activateService(identity, ownDependencies.toArray(new RequiredService<?>[ownDependencies.size()])),
                    identity,
                    context);
        }

        /**
         * Determines whether the service activation and registration is allowed.
         * <p>
         *     In the default implementation this method always returns {@literal true}.
         * @return {@literal true}, if registration is allowed; otherwise, {@literal false}.
         */
        @MethodStub
        protected boolean isActivationAllowed(){
            return true;
        }

        private void register(final BundleContext context, final ActivationPropertyReader properties) throws Exception {
            this.properties = properties;
            if(isActivationAllowed()) {
                if (ownDependencies.isEmpty()) //instantiate and register service now because there are no dependencies
                    activateAndRegisterService(context);
                else {
                    final DependencyListeningFilter filter = new DependencyListeningFilter();
                    for (final RequiredService<?> dependency : ownDependencies) {
                        filter.append(dependency);
                        for (final ServiceReference<?> serviceRef : dependency.getCandidates(context))
                            serviceChanged(context, new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
                    }
                    //dependency tracking required
                    filter.applyServiceListener(context, this);
                }
            }
            else this.properties = emptyActivationPropertyReader;
        }

        private void unregister(final BundleContext context) throws Exception {
            if (!ownDependencies.isEmpty()) context.removeServiceListener(this);
            //cancels registration
            T serviceInstance = null;
            try {
                if (registration != null) {
                    serviceInstance = registration.get();
                    registration.unregister();
                }
                else serviceInstance = null;
            } catch (final IllegalStateException ignored) {
                //unregister can throws this exception and it must be suppressed
            } finally {
                registration = null;
            }

            //release service instance
            try {
                if (serviceInstance != null)
                    cleanupService(serviceInstance, true);
            } finally {
                //releases all dependencies
                for (final RequiredService<?> dependency : ownDependencies)
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
            if(serviceInstance instanceof AutoCloseable)
                ((AutoCloseable)serviceInstance).close();
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
        private static final long serialVersionUID = 6353271076932722292L;

        private synchronized <V, E extends Exception> V synchronizedInvoke(final ExceptionalCallable<V, E> action) throws E{
            return action.call();
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
        private final LazyValue<String> factoryPID;

        /**
         * Initializes a new holder for the provided service.
         *
         * @param dependencies A collection of service dependencies of dynamic services.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        protected DynamicServiceManager(final RequiredService<?>... dependencies) {
            super(ManagedServiceFactory.class, dependencies);
            factoryPID = LazyContainers.NORMAL.create(this::getFactoryPIDImpl);
        }

        private String getFactoryPIDImpl(){
            return getFactoryPID(ArrayUtils.toArray(super.ownDependencies, RequiredService.class));
        }

        /**
         * Gets the base persistent identifier used as a prefix for individual dynamic services configuration.
         * @param dependencies A set of dependencies required by this service manager.
         * @return The base persistent identifier. Cannot be {@literal null} or empty string.
         */
        protected abstract String getFactoryPID(final RequiredService<?>... dependencies);

        /**
         * Automatically invokes by SNAMP when the dynamic service should be updated with
         * a new configuration.
         * <p>
         *     Don't worry about synchronization of this method because
         *     SNAMP infrastructure will call this method in synchronized context.
         * @param service The service to be updated.
         * @param configuration A new configuration of the service.
         * @param dependencies A collection of dependencies required for the service.
         * @return An updated service.
         * @throws Exception Unable to create new service or update the existing service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        protected abstract TService updateService(final TService service,
                                                  final Dictionary<String, ?> configuration,
                                                  final RequiredService<?>... dependencies) throws Exception;

        /**
         * Automatically invokes by SNAMP when the new dynamic service should be created.
         * <p>
         *     Don't worry about synchronization of this method because
         *     SNAMP infrastructure will call this method in synchronized context.
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
         * <p>
         *     Don't worry about synchronization of this method because
         *     SNAMP infrastructure will call this method in synchronized context.
         * @param service A service to dispose.
         * @throws Exception Unable to dispose the service.
         */
        protected abstract void dispose(final TService service, final boolean bundleStop) throws Exception;

        /**
         * Log error details when {@link #updateService(Object, java.util.Dictionary, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])} failed.
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

        LogicalOperation createLogicalOperationForUpdate(final String servicePID){
            return DynamicServiceLogicalOperation.update(factoryPID.get(), servicePID);
        }

        LogicalOperation createLogicalOperationForDelete(final String servicePID){
            return DynamicServiceLogicalOperation.delete(factoryPID.get(), servicePID);
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
            final String factoryPID = this.factoryPID.get();
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
                    final LogicalOperation logger = createLogicalOperationForUpdate(pid);
                    try {
                        service = containsKey(pid) ?
                                updateService(get(pid), properties, dependencies) :
                                activateService(pid, properties, dependencies);
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
                    final LogicalOperation logger = createLogicalOperationForDelete(pid);
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
            serviceInstance.synchronizedInvoke((ExceptionalCallable<Void, Exception>) () -> {
                try {
                    for (final TService service : serviceInstance.values())
                        dispose(service, stopBundle);
                } finally {
                    serviceInstance.clear();
                }
                return null;
            });
            destroyManager();
        }

        @MethodStub
        protected void destroyManager() throws Exception{

        }
    }

    private static final class ServiceRegistrationHolder<S, T extends S> implements ServiceRegistration<S>, Supplier<T>{
        private final ServiceRegistration<S> registration;
        private T serviceInstance;

        private ServiceRegistrationHolder(final Class<S> serviceContract,
                                          final T service,
                                          final Dictionary<String, ?> identity,
                                          final BundleContext context) {
            serviceInstance = Objects.requireNonNull(service);
            registration = context.registerService(serviceContract, service, identity);
        }

        @Override
        public void setProperties(final Dictionary<String, ?> properties) {
            registration.setProperties(properties);
        }

        private Dictionary<String, ?> dumpProperties(){
            final String[] propertyNames = registration.getReference().getPropertyKeys();
            final Dictionary<String, Object> result = new Hashtable<>(propertyNames.length * 2);
            for(final String propertyName: propertyNames)
                result.put(propertyName, registration.getReference().getProperty(propertyName));
            return result;
        }

        @Override
        public T get() {
            return serviceInstance;
        }

        @Override
        public ServiceReference<S> getReference(){
            return registration.getReference();
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

        private boolean isUnregistered() {
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

        @Override
        final LogicalOperation createLogicalOperationForUpdate(final String servicePID) {
            return SubRegistryLogicalOperation.update(super.factoryPID.get(), servicePID, serviceContract);
        }

        @Override
        final LogicalOperation createLogicalOperationForDelete(final String servicePID) {
            return SubRegistryLogicalOperation.delete(super.factoryPID.get(), servicePID, serviceContract);
        }

        /**
         * Updates the service with a new configuration.
         * @param service The service to update.
         * @param configuration A new configuration of the service.
         * @param dependencies A collection of dependencies required for the service.
         * @return The updated service.
         * @throws Exception Unable to update service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @ThreadSafe
        protected abstract T update(final T service,
                                    final Dictionary<String, ?> configuration,
                                    final RequiredService<?>... dependencies) throws Exception;

        /**
         * Automatically invokes by SNAMP when the dynamic service should be updated with
         * a new configuration.
         *
         * @param registration The service to be updated.
         * @param configuration        A new configuration of the service.
         * @param dependencies A collection of dependencies required for the service.
         * @return An updated service.
         * @throws Exception                                  Unable to create new service or update the existing service.
         * @throws org.osgi.service.cm.ConfigurationException Invalid service configuration.
         */
        @Override
        @ThreadSafe
        protected final ServiceRegistrationHolder<S, T> updateService(ServiceRegistrationHolder<S, T> registration,
                                                                      final Dictionary<String, ?> configuration,
                                                                      final RequiredService<?>... dependencies) throws Exception {
            final T oldService = registration.get();
            final T newService = update(oldService, configuration, dependencies);
            if (newService == null) {
                dispose(registration);
                registration = null;
            }
            else if (oldService != newService) {
                //save the identity of the service and removes registration of the previous version of service
                final Dictionary<String, ?> identity = dispose(registration);
                registration = new ServiceRegistrationHolder<>(serviceContract, newService, identity, getBundleContextOfObject(this));
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
        @ThreadSafe
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
        @ThreadSafe
        protected final ServiceRegistrationHolder<S, T> activateService(final String servicePID,
                                                                        final Dictionary<String, ?> configuration,
                                                                        final RequiredService<?>... dependencies) throws Exception {
            final Hashtable<String, Object> identity = new Hashtable<>(4);
            identity.put(Constants.SERVICE_PID, servicePID);
            final T service = createService(identity, configuration, dependencies);
            return service != null ? new ServiceRegistrationHolder<>(serviceContract, service, identity, getBundleContextOfObject(this)) : null;
        }

        /**
         * Releases all resources associated with the service instance.
         * @param service A service to dispose.
         * @param identity Service identity.
         * @throws Exception Unable to dispose service.
         */
        @ThreadSafe
        protected abstract void cleanupService(final T service,
                                               final Dictionary<String, ?> identity) throws Exception;

        private Dictionary<String, ?> dispose(final ServiceRegistrationHolder<S, T> registration) throws Exception{
            final T serviceInstance = registration.get();
            final Dictionary<String, ?> properties = registration.dumpProperties();
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
     * @version 1.2
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
                     final RequiredService<?>... bundleLevelDependencies);
    }

    private static final class ListOfProvidedServices extends ArrayList<ProvidedService<?, ?>> implements ProvidedServices{
        private static final long serialVersionUID = 341418339520679799L;

        private ListOfProvidedServices(final ProvidedService<?, ?>... services){
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
        this.providedServices = new LinkedList<>();
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
    protected final synchronized void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
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
    protected final synchronized void activate(final BundleContext context,
                                  final ActivationPropertyPublisher activationProperties,
                                  final RequiredService<?>... dependencies) throws Exception {
        activate(activationProperties, dependencies);
        serviceRegistry.provide(providedServices, getActivationProperties(), dependencies);
        for (final ProvidedService<?, ?> service : providedServices)
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
    protected final synchronized void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
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
    protected final synchronized void shutdown(final BundleContext context) throws Exception {
        shutdown();
    }

    /**
     * Releases all resources associated with this library.
     * @throws Exception Abnormal library termination.
     */
    @MethodStub
    protected void shutdown() throws Exception{

    }
}
