package com.bytex.snamp.core;

import com.bytex.snamp.*;
import com.bytex.snamp.io.IOUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.bytex.snamp.MethodStub;
import org.osgi.framework.*;

import java.util.*;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;

/**
 * Represents an abstract for all SNAMP-specific bundle activators.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractBundleActivator implements BundleActivator, ServiceListener {

    final static class BundleLogicalOperation extends RichLogicalOperation {
        static final String BUNDLE_NAME_PROPERTY = "bundleName";

        private BundleLogicalOperation(final Logger logger,
                                       final String operationName,
                                       final BundleContext context) {
            super(logger,
                    operationName,
                    ImmutableMap.of(BUNDLE_NAME_PROPERTY, context.getBundle().getSymbolicName()));
        }

        private static BundleLogicalOperation startBundle(final Logger logger, final BundleContext context) {
            return new BundleLogicalOperation(logger, "startBundle", context);
        }

        private static BundleLogicalOperation stopBundle(final Logger logger, final BundleContext context) {
            return new BundleLogicalOperation(logger, "stopBundle", context);
        }

        private static BundleLogicalOperation processServiceChanged(final Logger logger, final BundleContext context) {
            return new BundleLogicalOperation(logger, "bundleServiceChanged", context);
        }
    }

    /**
     * Represents bundle activation property.
     * @param <T> Type of the activation property.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface ActivationProperty<T> extends Attribute<T>{
        /**
         * Gets type of the activation property.
         * @return The type of the attribute value.
         */
        @Override
        TypeToken<T> getType();

        /**
         * Gets default value of this property.
         * @return Default value of this property.
         */
        @Override
        T getDefaultValue();
    }

    /**
     * Represents named activation property.
     * @param <T> Type of the property.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface NamedActivationProperty<T> extends ActivationProperty<T>{
        /**
         * Gets name of this property.
         * @return The name of this property.
         */
        String getName();
    }

    /**
     * Represents publisher for the activation properties.
     * <p>
     *      You should not implement this interface directly in your code.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface ActivationPropertyPublisher{
        /**
         * Publishes the activation property.
         * @param propertyDef The definition of the property. Cannot be {@literal null}.
         * @param value The value of the property.
         * @param <T> Type of the property value.
         * @return {@literal true}, if the property is published successfully and
         * there is no duplications; otherwise, {@literal false}.
         */
        <T> boolean publish(final ActivationProperty<T> propertyDef, final T value);
    }

    /**
     * Represents activation properties reader.
     * <p>
     *     You should not implement this interface directly in your code.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected interface ActivationPropertyReader extends AttributeReader{
        /**
         * Finds the property definition.
         * @param propertyType The type of the property definition.
         * @param filter Property definition filter.
         * @param <P> The type of the property definition.
         * @return The property definition; or {@literal null}, if porperty not found.
         */
        <P extends ActivationProperty<?>> P getProperty(final Class<P> propertyType, final Predicate<P> filter);
    }

    /**
     * Represents an empty activation property reader.
     */
    protected static final ActivationPropertyReader emptyActivationPropertyReader = new ActivationPropertyReader() {
        @Override
        public <T> T getValue(final Attribute<T> propertyDef) {
            return null;
        }

        @Override
        public <P extends ActivationProperty<?>> P getProperty(final Class<P> propertyType, final Predicate<P> filter) {
            return null;
        }
    };

    private static final class ActivationProperties extends HashMap<ActivationProperty<?>, Object> implements ActivationPropertyPublisher, ActivationPropertyReader{
        private static final long serialVersionUID = -1855442064833049167L;

        private ActivationProperties(){
            super(10);
        }

        /**
         * Publishes the activation property.
         *
         * @param propertyDef The definition of the property. Cannot be {@literal null}.
         * @param value       The value of the property.
         * @return {@literal true}, if the property is published successfully and
         * there is no duplications; otherwise, {@literal false}.
         * @throws IllegalArgumentException propertyDef is {@literal null}.
         */
        @Override
        public <T> boolean publish(final ActivationProperty<T> propertyDef, final T value) {
            if(propertyDef == null) return false;
            else if(containsKey(propertyDef)) return false;
            else {
                put(propertyDef, value);
                return true;
            }
        }

        /**
         * Reads value of the activation property.
         *
         * @param propertyDef The definition of the activation property.
         * @return The property value; or {@literal null}, if property doesn't exist.
         */
        @Override
        public <T> T getValue(final Attribute<T> propertyDef) {
            if (propertyDef == null) return null;
            else if (containsKey(propertyDef)) {
                final Object value = get(propertyDef);
                return TypeTokens.isInstance(value, propertyDef.getType()) ?
                        TypeTokens.cast(value, propertyDef.getType()) :
                        propertyDef.getDefaultValue();
            } else return null;
        }

        /**
         * Finds the property definition.
         *
         * @param propertyType The type of the property definition.
         * @param filter       Property definition filter.
         * @return The property definition; or {@literal null}, if property not found.
         */
        @Override
        public <P extends ActivationProperty<?>> P getProperty(final Class<P> propertyType, final Predicate<P> filter) {
            if(propertyType == null || filter == null) return null;
            for(final ActivationProperty<?> prop: keySet())
                if(propertyType.isInstance(prop)){
                    final P candidate = propertyType.cast(prop);
                    if(filter.apply(candidate)) return candidate;
                }
            return null;
        }
    }

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
         */
        protected abstract void bind(final S serviceInstance,
                                     final Dictionary<String, ?> properties);

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
        @SuppressWarnings("UnusedParameters")
        @MethodStub
        protected void update(final Dictionary<String, ?> properties){

        }

        /**
         * Determines whether the dependency is resolved.
         * @return {@literal true}, if this dependency is resolved and reference to the service
         * is caught; otherwise, {@literal false}.
         */
        public final boolean isResolved(){
            return reference != null;
        }

        private boolean matchExact(final ServiceReference<?> candidate){
            return isInstanceOf(candidate, dependencyContract) && match(candidate);
        }

        private boolean bind(final BundleContext context, final ServiceReference<?> reference){
            if(isResolved()) return false;
            else if(matchExact(reference))
                try {
                    bind(dependencyContract.cast(context.getService(reference)), getProperties(reference));
                    return true;
                }
                finally {
                    this.reference = reference;
                }
            else return false;
        }

        private boolean unbind(final BundleContext context,
                               final ServiceReference<?> reference){
            if(!isResolved()) return false;
            else if(matchExact(reference))
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

        final boolean unbind(final BundleContext context){
            return unbind(context, this.reference);
        }

        private boolean update(final ServiceReference<?> reference) {
            if(isResolved()){
                update(getProperties(reference));
                return true;
            }
            return false;
        }

        final synchronized void processServiceEvent(final BundleContext context,
                                              final ServiceReference<?> reference,
                                              final int eventType) {
            switch (eventType){
                case ServiceEvent.REGISTERED:
                    bind(context, reference);
                    return;
                case ServiceEvent.UNREGISTERING:
                case ServiceEvent.MODIFIED_ENDMATCH:
                    unbind(context, reference);
                    return;
                case ServiceEvent.MODIFIED:
                    update(reference);
            }
        }


        final ServiceReference<?>[] getCandidates(final BundleContext context) {
            ServiceReference<?>[] refs;
            try {
                refs = context.getAllServiceReferences(dependencyContract.getName(), null);
            }
            catch (final InvalidSyntaxException e) {
                refs = null;
            }
            return refs != null ? refs : ArrayUtils.emptyArray(ServiceReference[].class);
        }
    }

    static final class DependencyListeningFilter {
        private int appendCalledTimes = 0;
        private final StringBuilder filter = new StringBuilder(64);

        void append(final RequiredService<?> dependency){
            IOUtils.append(filter, "(%s=%s)", Constants.OBJECTCLASS, dependency.dependencyContract.getName());
            appendCalledTimes += 1;
        }

        void applyServiceListener(final BundleContext context, final ServiceListener listener) throws InvalidSyntaxException {
            final String filter = toString();
            if(filter.isEmpty())
                context.addServiceListener(listener);
            else context.addServiceListener(listener, filter);
        }

        @Override
        public String toString() {
            switch (appendCalledTimes){
                case 0: return "";
                case 1: return filter.toString();
                default:
                    return String.format("(|%s)", filter);
            }
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
         */
        @Override
        protected void bind(final S serviceInstance,
                            final Dictionary<String, ?> properties) {
            this.serviceInstance = serviceInstance;
        }

        /**
         * Informs this dependency about detaching dependency.
         */
        @Override
        protected void unbind() {
            serviceInstance = null;
        }
    }

    /**
     * Represents simple dependency descriptor.
     * <p>
     *     This class describes service dependency based on service contract only.
     *     No additional filters supplied.
     * </p>
     * @param <S> Type of the required service contract.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class SimpleDependency<S> extends RequiredServiceAccessor<S>{
        /**
         * Initializes a new simple dependency descriptor.
         * @param serviceContract The type of the service contract.
         */
        public SimpleDependency(final Class<S> serviceContract){
            super(serviceContract);
        }

        /**
         * Provides matching reference to the conditions of this dependency.
         * <p>
         *     This method always returns {@literal true}.
         * </p>
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
     * Represents activation state of the bundle.
     * <p>
     *     Activation is additional lifecycle on top of the bundle's lifecycle
     *     and reflects dependency resolving annotations.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
     protected enum ActivationState {
        /**
         * Bundle is not activated.
         */
        NOT_ACTIVATED,
        /**
         * Bundle is activating.
         */
        ACTIVATING,

        /**
         * Bundle is activated.
         */
        ACTIVATED,

        /**
         * Bundle is deactivating.
         */
        DEACTIVATING
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
        return findDependency(descriptor, serviceContract, Arrays.asList(dependencies));
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
    public static <S, D extends RequiredService<S>> D findDependency(final Class<D> descriptor, final Class<S> serviceContract, final Iterable<RequiredService<?>> dependencies){
        for(final RequiredService<?> dependency: dependencies)
            if(descriptor.isInstance(dependency) && Objects.equals(dependency.dependencyContract, serviceContract))
                return descriptor.cast(dependency);
        return null;
    }

    /**
     * Defines activation property.
     * @param propertyType The type of the property.
     * @param defaultValue The default value of the property.
     * @param <T> Type of the property.
     * @return Activation property definition.
     */
    protected static <T> ActivationProperty<T> defineActivationProperty(final Class<T> propertyType, final T defaultValue) {
        return defineActivationProperty(TypeToken.of(propertyType), defaultValue);
    }

    /**
     * Defines activation property.
     * @param propertyType The type of the property.
     * @param defaultValue The default value of the property.
     * @param <T> Type of the property.
     * @return Activation property definition.
     */
    protected static <T> ActivationProperty<T> defineActivationProperty(final TypeToken<T> propertyType, final T defaultValue){
        return new ActivationProperty<T>() {
            private static final long serialVersionUID = -2754311111835732097L;

            @Override
            public TypeToken<T> getType() {
                return propertyType;
            }

            @Override
            public T getDefaultValue() {
                return defaultValue;
            }
        };
    }

    /**
     * Defines activation property without default value.
     * @param propertyType The type of the property.
     * @param <T> The type of the property.
     * @return Activation property definition.
     */
    protected static <T> ActivationProperty<T> defineActivationProperty(final Class<T> propertyType){
        return defineActivationProperty(TypeToken.of(propertyType));
    }

    /**
     * Defines activation property without default value.
     * @param propertyType The type of the property.
     * @param <T> The type of the property.
     * @return Activation property definition.
     */
    protected static <T> ActivationProperty<T> defineActivationProperty(final TypeToken<T> propertyType){
        return defineActivationProperty(propertyType, null);
    }

    /**
     * Defines named activation property.
     * @param propertyName The name of the property.
     * @param propertyType The type of the property.
     * @param defaultValue The default value of the property.
     * @param <T> Type of the property.
     * @return Named activation property definition.
     */
    protected static <T> NamedActivationProperty<T> defineActivationProperty(final String propertyName, final Class<T> propertyType, final T defaultValue){
        return new NamedActivationProperty<T>() {
            private static final long serialVersionUID = -5801941853707054641L;
            private final TypeToken<T> pType = TypeToken.of(propertyType);

            @Override
            public String getName() {
                return propertyName;
            }

            @Override
            public TypeToken<T> getType() {
                return pType;
            }

            @Override
            public T getDefaultValue() {
                return defaultValue;
            }

            @Override
            public int hashCode() {
                return propertyName.hashCode();
            }

            @Override
            public boolean equals(final Object obj) {
                if(obj instanceof NamedActivationProperty<?>)
                    return Objects.equals(propertyName, ((NamedActivationProperty<?>)obj).getName());
                else return Objects.equals(propertyName, obj);
            }
        };
    }

    /**
     * Defines named activation property without default value.
     * @param propertyName The name of the property.
     * @param propertyType The type of the property.
     * @param <T> Type of the property.
     * @return Named activation property definition.
     */
    protected static <T> NamedActivationProperty<T> defineActivationProperty(final String propertyName, final Class<T> propertyType){
        return defineActivationProperty(propertyName, propertyType, null);
    }

    /**
     * Finds dependency by its required service contract.
     * @param serviceContract The service contract required by dependency.
     * @param dependencies A collection of dependencies.
     * @param <S> Type of the service contract.
     * @return Search result; or {@literal null} if dependency not found.
     */
    @SuppressWarnings("unchecked")
    public static <S> RequiredService<S> findDependency(final Class<S> serviceContract, final RequiredService<?>... dependencies){
        return findDependency(serviceContract, Arrays.asList(dependencies));
    }

    /**
     * Finds dependency by its required service contract.
     * @param serviceContract The service contract required by dependency.
     * @param dependencies A collection of dependencies.
     * @param <S> Type of the service contract.
     * @return Search result; or {@literal null} if dependency not found.
     */
    @SuppressWarnings("unchecked")
    public static <S> RequiredService<S> findDependency(final Class<S> serviceContract, final Iterable<RequiredService<?>> dependencies){
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
    public static <S, D extends RequiredServiceAccessor<S>> S getDependency(final Class<D> descriptor, final Class<S> serviceContract, final RequiredService<?>... dependencies){
        return getDependency(descriptor, serviceContract, Arrays.asList(dependencies));
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
    public static <S, D extends RequiredServiceAccessor<S>> S getDependency(final Class<D> descriptor, final Class<S> serviceContract, final Iterable<RequiredService<?>> dependencies){
        final D found = findDependency(descriptor, serviceContract, dependencies);
        return found != null ? found.getService() : null;
    }

    /**
     * Represents an empty array of required services.
     */
    protected static final RequiredService<?>[] EMPTY_REQUIRED_SERVICES = ArrayUtils.emptyArray(RequiredService[].class);

    private final List<RequiredService<?>> bundleLevelDependencies;
    private final ActivationProperties properties;
    private ActivationState state;

    /**
     * Initializes a new bundle activator in {@link com.bytex.snamp.core.AbstractBundleActivator.ActivationState#NOT_ACTIVATED} state.
     */
    protected AbstractBundleActivator(){
        bundleLevelDependencies = new ArrayList<>(5);
        state = ActivationState.NOT_ACTIVATED;
        properties = new ActivationProperties();
    }

    /**
     * Gets state of this activator.
     * @return The state of this activator.
     * @see com.bytex.snamp.core.AbstractBundleActivator.ActivationState
     */
    protected final ActivationState getState(){
        return state;
    }

    private synchronized void serviceChanged(final BundleContext context, final ServiceEvent event){
        if(state == ActivationState.ACTIVATING) return;
        int resolvedDependencies = 0;
        for(final RequiredService<?> dependency: bundleLevelDependencies) {
            dependency.processServiceEvent(context, event.getServiceReference(), event.getType());
            if(dependency.isResolved()) resolvedDependencies += 1;
        }
        switch (state){
            case ACTIVATED: //dependency lost but bundle is activated
                if(resolvedDependencies != bundleLevelDependencies.size())
                    try {
                        state = ActivationState.DEACTIVATING;
                        deactivateInternal(context, properties);
                    }
                    catch (final Exception e) {
                        deactivationFailure(e, properties);
                    }
                    finally {
                        state = ActivationState.NOT_ACTIVATED;
                    }
                return;
            case NOT_ACTIVATED:    //dependencies resolved but bundle is not activated
                if(resolvedDependencies == bundleLevelDependencies.size())
                    try {
                        state = ActivationState.ACTIVATING;
                        activate(context,
                                properties,
                                bundleLevelDependencies.toArray(new RequiredService<?>[resolvedDependencies]));
                        state = ActivationState.ACTIVATED;
                    }
                    catch (final Exception e) {
                        activationFailure(e, properties);
                        state = ActivationState.NOT_ACTIVATED;
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
        final BundleContext context = getBundleContextOfObject(this);
        try(final LogicalOperation ignored = BundleLogicalOperation.processServiceChanged(getLogger(), context)) {
            serviceChanged(context, event);
        }
    }

    /**
     * Starts the bundle.
     * @param context The execution context of the bundle being started.
     * @throws Exception An exception occurred during bundle starting.
     */
    @Override
    public final void start(final BundleContext context) throws Exception {
        try (final LogicalOperation ignored = BundleLogicalOperation.startBundle(getLogger(), context)) {
            start(context, bundleLevelDependencies);
            final DependencyListeningFilter filter = new DependencyListeningFilter();
            //try to resolve bundle-level dependencies immediately
            for (final RequiredService<?> dependency : bundleLevelDependencies) {
                filter.append(dependency);
                for (final ServiceReference<?> serviceRef : dependency.getCandidates(context))
                    serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
            }
            //attach bundle-level dependencies as service listeners
            filter.applyServiceListener(context, this);
        }
    }

    private static void unregister(final BundleContext context, final Collection<RequiredService<?>> dependencies){
        for(final RequiredService<?> dependency: dependencies)
            dependency.unbind(context);
    }

    private void deactivateInternal(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        unregister(context, bundleLevelDependencies);
        deactivate(context, activationProperties);
    }

    /**
     * Gets logger associated with this activator.
     * @return The logger associated with this activator.
     */
    protected Logger getLogger(){
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Stops the bundle.
     * @param context The execution context of the bundle being stopped.
     * @throws Exception An exception occurred during bundle stopping.
     */
    @Override
    public final void stop(final BundleContext context) throws Exception {
        try (final LogicalOperation ignored = BundleLogicalOperation.stopBundle(getLogger(), context)) {
            context.removeServiceListener(this);
            if (state == ActivationState.ACTIVATED) {
                state = ActivationState.DEACTIVATING;
                deactivateInternal(context, getActivationProperties());
            }
            try {
                shutdown(context);
            } finally {
                //unbind all dependencies
                bundleLevelDependencies.clear();
                properties.clear();
            }
        }
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     * @param context The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    protected abstract void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception;

    /**
     * Activates the bundle.
     * <p>
     *     This method will be called when all bundle-level dependencies will be resolved.
     * </p>
     * @param context The execution context of the bundle being activated.
     * @param activationProperties A collection of bundle's activation properties to fill.
     * @param dependencies A collection of resolved dependencies.
     * @throws Exception An exception occurred during activation.
     */
    protected abstract void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, RequiredService<?>... dependencies) throws Exception;

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, com.bytex.snamp.core.AbstractBundleActivator.ActivationPropertyPublisher, com.bytex.snamp.core.AbstractBundleActivator.RequiredService[])}  method.
     * @param e An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @SuppressWarnings("UnusedParameters")
    @MethodStub
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties){

    }

    /**
     * Handles an exception thrown by {@link } method.
     * @param e An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @SuppressWarnings("UnusedParameters")
    @MethodStub
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties){

    }

    final ActivationPropertyReader getActivationProperties(){
        return properties;
    }

    /**
     * Deactivates the bundle.
     * <p>
     *     This method will be called when at least one bundle-level dependency will be lost.
     * </p>
     * @param context The execution context of the bundle being deactivated.
     * @param activationProperties A collection of activation properties to read.
     * @throws Exception An exception occurred during bundle deactivation.
     */
    protected abstract void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception;

    /**
     * Stops the bundle.
     * @param context The execution context of the bundle being stopped.
     * @throws Exception An exception occurred during bundle stopping.
     */
    protected abstract void shutdown(final BundleContext context) throws Exception;

    /**
     * Returns the value of the specified property. If the key is not found in
     * the Framework properties, the system properties are then searched. The
     * method returns {@code null} if the property is not found.
     *
     * <p>
     * All bundles must have permission to read properties whose names start
     * with &quot;org.osgi.&quot;.
     *
     * @param propertyName The name of the requested property.
     * @return The value of the requested property, or {@code null} if the
     *         property is undefined.
     **/
    protected final String getFrameworkProperty(final String propertyName){
        return getBundleContextOfObject(this).getProperty(propertyName);
    }

    /**
     * Gets properties of the service that is represented by the specified reference.
     * <p>
     *     Note that the service reference should be valid.
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
