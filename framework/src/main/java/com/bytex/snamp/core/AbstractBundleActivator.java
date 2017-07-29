package com.bytex.snamp.core;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Convert;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.ResettableIterator;
import com.bytex.snamp.internal.Utils;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents an abstract for all SNAMP-specific bundle activators.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public abstract class AbstractBundleActivator implements BundleActivator, ServiceListener {

    private final static class BundleLoggingScope extends LoggingScope {

        private BundleLoggingScope(final AbstractBundleActivator requester,
                                   final String operationName) {
            super(requester, operationName);
        }

        private static BundleLoggingScope startBundle(final AbstractBundleActivator requester) {
            return new BundleLoggingScope(requester, "startBundle");
        }

        private static BundleLoggingScope stopBundle(final AbstractBundleActivator requester) {
            return new BundleLoggingScope(requester, "stopBundle");
        }

        private static BundleLoggingScope processServiceChanged(final AbstractBundleActivator requester) {
            return new BundleLoggingScope(requester, "bundleServiceChanged");
        }
    }

    /**
     * Represents bundle activation property.
     * @param <T> Type of the activation property.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    protected interface ActivationProperty<T> {
        /**
         * Gets type of the activation property.
         * @return The type of the attribute value.
         */
        @Nonnull
        TypeToken<T> getType();

        /**
         * Gets default value of this property.
         * @return Default value of this property.
         */
        @Nullable
        default T getDefaultValue(){
            return null;
        }
    }

    /**
     * Represents publisher for the activation properties.
     * <p>
     *      You should not implement this interface directly in your code.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
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
        <T> boolean publish(@Nonnull final ActivationProperty<T> propertyDef, final T value);
    }

    /**
     * Represents activation properties reader.
     * <p>
     *     You should not implement this interface directly in your code.
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    protected interface ActivationPropertyReader{
        <T> T getProperty(@Nonnull final ActivationProperty<T> propertyDef);

        /**
         * Finds the property definition.
         * @param propertyType Type of requested property. Cannot be {@literal null}.
         * @param filter Property definition filter. Cannot be {@literal null}.
         * @param <P> The type of the property definition.
         * @return The property definition; or {@literal null}, if porperty not found.
         */
        <P extends ActivationProperty<?>> Optional<P> findProperty(@Nonnull final Class<P> propertyType, @Nonnull final Predicate<? super P> filter);
    }

    /**
     * Represents an empty activation property reader.
     */
    static final ActivationPropertyReader EMPTY_ACTIVATION_PROPERTY_READER = new ActivationPropertyReader() {
        @Override
        public <T> T getProperty(@Nonnull final ActivationProperty<T> propertyDef) {
            return null;
        }

        @Override
        public <P extends ActivationProperty<?>> Optional<P> findProperty(@Nonnull final Class<P> propertyType, @Nonnull final Predicate<? super P> filter) {
            return Optional.empty();
        }
    };

    private static final class ActivationProperties extends HashMap<ActivationProperty<?>, Object> implements ActivationPropertyPublisher, ActivationPropertyReader{
        private static final long serialVersionUID = -1855442064833049167L;

        private ActivationProperties(){
            super(10);
        }

        @Override
        public <T> boolean publish(@Nonnull final ActivationProperty<T> propertyDef, final T value) {
            return putIfAbsent(propertyDef, value) == null;
        }

        @Override
        public <T> T getProperty(@Nonnull final ActivationProperty<T> propertyDef) {
            return Convert.toType(get(propertyDef), propertyDef.getType()).orElseGet(propertyDef::getDefaultValue);
        }

        @Override
        public <P extends ActivationProperty<?>> Optional<P> findProperty(@Nonnull final Class<P> propertyType, @Nonnull final Predicate<? super P> filter) {
            return keySet().stream()
                    .filter(propertyType::isInstance)
                    .map(propertyType::cast)
                    .filter(filter)
                    .findFirst();
        }
    }

    /**
     * Represents dependency descriptor.
     * @param <S> Type of the required service.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    public static abstract class RequiredService<S> {
        final Class<S> dependencyContract;
        ServiceReference<S> reference;

        /**
         * Initializes a new dependency descriptor.
         * @param dependencyType Contract of the required service. Cannot be {@literal null}.
         */
        protected RequiredService(@Nonnull final Class<S> dependencyType) {
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
        protected abstract boolean match(final ServiceReference<S> reference);

        /**
         * Informs this dependency about resolving dependency.
         * @param serviceRef A reference to resolved service.
         */
        protected abstract void bind(final ServiceReference<S> serviceRef);

        /**
         * Informs this dependency about detaching dependency.
         */
        protected abstract void unbind();

        /**
         * Informs this dependency about modification of the service properties.
         * <p>
         *     In the default implementation this method does nothing.
         * </p>
         * @param serviceRef Updated reference to a service.
         */
        @MethodStub
        protected void update(final ServiceReference<S> serviceRef){

        }

        /**
         * Determines whether the dependency is resolved.
         * @return {@literal true}, if this dependency is resolved and reference to the service
         * is caught; otherwise, {@literal false}.
         */
        public final boolean isResolved(){
            return reference != null;
        }

        private void processServiceEventImpl(final ServiceReference<S> reference,
                                         final int eventType) {
            switch (eventType) {
                case ServiceEvent.REGISTERED:
                    if (!isResolved() && match(reference)) {
                        bind(this.reference = reference);
                    }
                    return;
                case ServiceEvent.UNREGISTERING:
                case ServiceEvent.MODIFIED_ENDMATCH:
                    if (isResolved())
                        try {
                            unbind();
                        } finally {
                            this.reference = null;
                        }
                    return;
                case ServiceEvent.MODIFIED:
                    update(reference);
            }
        }

        @SuppressWarnings("unchecked")
        final synchronized boolean processServiceEvent(final ServiceReference<?> reference,
                                       final int eventType) {
            if (Utils.isInstanceOf(reference, dependencyContract))
                processServiceEventImpl((ServiceReference<S>) reference, eventType);
            return isResolved();
        }

        final ServiceReference<S>[] getCandidates(final BundleContext context) {
            return new DefaultServiceSelector()
                    .setServiceType(dependencyContract)
                    .getServiceReferences(context, dependencyContract);
        }
    }

    @NotThreadSafe
    static final class DependencyListeningFilterBuilder {
        private int appendCalledTimes = 0;
        private final StringBuilder filter = new StringBuilder(64);

        void append(final RequiredService<?> dependency) {
            filter.append('(').append(Constants.OBJECTCLASS).append('=').append(dependency.dependencyContract.getName()).append(')');
            appendCalledTimes += 1;
        }

        void applyServiceListener(final BundleContext context, final ServiceListener listener) throws InvalidSyntaxException {
            final String filter = toString();
            if(filter.isEmpty())
                context.addServiceListener(listener);
            else
                context.addServiceListener(listener, filter);
        }

        @Override
        public String toString() {
            switch (appendCalledTimes) {
                case 0:
                    return "";
                case 1:
                    return filter.toString();
                default:
                    return "(|" + filter + ')';
            }
        }
    }

    /**
     * Provides an accessor for the required service.
     * @param <S> Contract of the required service.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    protected static abstract class RequiredServiceAccessor<S> extends RequiredService<S>{
        private S serviceInstance;
        private final BundleContext context;

        RequiredServiceAccessor(@Nonnull final Class<S> dependencyType,
                                @Nonnull final BundleContext context) {
            super(dependencyType);
            this.context = context;
        }

        /**
         * Initializes a new dependency descriptor.
         *
         * @param dependencyType Contract of the required service. Cannot be {@literal null}.
         */
        protected RequiredServiceAccessor(@Nonnull final Class<S> dependencyType){
            super(dependencyType);
            this.context = Utils.getBundleContextOfObject(this);
        }

        /**
         * Gets a required service if this dependency is in resolved state.
         * @return A required service if this dependency is in resolved state.
         * @see #isResolved()
         */
        public final Optional<S> getService() {
            return Optional.ofNullable(serviceInstance);
        }

        protected void bind(final S service){

        }

        /**
         * Informs this dependency about resolving dependency.
         *
         * @param serviceRef A reference to resolved service.
         */
        @Override
        protected final void bind(final ServiceReference<S> serviceRef) {
            serviceInstance = context.getService(serviceRef);
            if (serviceInstance == null) //failed to resolve service
                reference = null;
        }

        protected void unbind(final S service){

        }

        /**
         * Informs this dependency about detaching dependency.
         */
        @Override
        protected final void unbind() {
            if (reference != null && serviceInstance != null)
                try {
                    unbind(serviceInstance);
                } finally {
                    context.ungetService(reference);
                    serviceInstance = null;
                }
        }
    }

    /**
     * Represents simple dependency descriptor.
     * @param <S> Type of the required service contract.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.1
     */
    private static final class SimpleDependency<S> extends RequiredServiceAccessor<S>{
        /**
         * Initializes a new simple dependency descriptor.
         * @param serviceContract The type of the service contract.
         * @param context Context of the dependency declarer. Cannot be {@literal null}.
         */
        SimpleDependency(@Nonnull final Class<S> serviceContract,
                         @Nonnull final BundleContext context){
            super(serviceContract, context);
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
        protected boolean match(final ServiceReference<S> reference) {
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
     * @version 2.1
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
     * Defines activation property.
     * @param propertyType The type of the property.
     * @param defaultValue The default value of the property.
     * @param <T> Type of the property.
     * @return Activation property definition.
     */
    protected static <T> ActivationProperty<T> defineActivationProperty(@Nonnull final Class<T> propertyType, @Nullable final T defaultValue) {
        return defineActivationProperty(TypeToken.of(propertyType), defaultValue);
    }

    /**
     * Defines activation property.
     * @param propertyType The type of the property.
     * @param defaultValue The default value of the property.
     * @param <T> Type of the property.
     * @return Activation property definition.
     */
    protected static <T> ActivationProperty<T> defineActivationProperty(@Nonnull final TypeToken<T> propertyType, @Nullable final T defaultValue){
        return new ActivationProperty<T>() {

            @Override
            @Nonnull
            public TypeToken<T> getType() {
                return propertyType;
            }

            @Override
            @Nullable
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
    protected static <T> ActivationProperty<T> defineActivationProperty(final TypeToken<T> propertyType) {
        return defineActivationProperty(propertyType, null);
    }

    /**
     * Represents collection of dependencies.
     * @author Roman Sakno
     * @since 2.0
     * @version 2.1
     */
    protected static final class DependencyManager implements Iterable<RequiredService<?>> {
        private boolean frozen;
        private final Collection<RequiredService<?>> dependencies;

        DependencyManager(final RequiredService<?>... dependencies) {
            frozen = false;
            this.dependencies = new LinkedList<>(Arrays.asList(dependencies));
        }

        /**
         * Finds dependency by its required service contract.
         * @param serviceContract The service contract required by dependency.
         * @param <S> Type of the service contract.
         * @return Search result; or {@literal null} if dependency not found.
         */
        @SuppressWarnings("unchecked")
        public <S> Optional<RequiredService<S>> getDependency(@Nonnull final Class<S> serviceContract,
                                                    @Nonnull final Predicate<? super RequiredService<S>> filter) {
            return dependencies.stream()
                    .filter(dependency -> dependency.dependencyContract.equals(serviceContract))
                    .map(dependency -> (RequiredService<S>) dependency)
                    .filter(filter)
                    .findFirst();
        }

        public <S, D extends RequiredService<S>> Optional<D> getDependency(@Nonnull final Class<D> dependencyType) {
            return dependencies.stream()
                    .filter(dependencyType::isInstance)
                    .map(dependencyType::cast)
                    .findFirst();
        }

        /**
         * Obtains a service from the collection of dependencies.
         * @param serviceContract The service contract required by dependency.
         * @param <S> Type of the service contract.
         * @return The resolved service; or {@literal null} if it is not available.
         */
        public <S> Optional<S> getService(final Class<S> serviceContract) {
            return getDependency(serviceContract, rs -> rs instanceof RequiredServiceAccessor<?>)
                    .map(RequiredServiceAccessor.class::cast)
                    .flatMap(RequiredServiceAccessor::getService);
        }

        public DependencyManager add(@Nonnull final Class<?> serviceType, @Nonnull final BundleContext context) {
            return add(new SimpleDependency<>(serviceType, context));
        }

        public DependencyManager add(@Nonnull final RequiredService<?> dependency){
            if(frozen)
                throw new IllegalStateException("New dependencies cannot be added");
            dependencies.add(Objects.requireNonNull(dependency));
            return this;
        }

        DependencyManager unbind(){
            dependencies.forEach(RequiredService::unbind);
            return this;
        }

        DependencyManager freeze(){
            frozen = true;
            return this;
        }

        DependencyManager defrost(){
            frozen = false;
            return this;
        }

        @Override
        @Nonnull
        public ResettableIterator<RequiredService<?>> iterator() {
            return ResettableIterator.of(dependencies, frozen);
        }

        @Override
        public void forEach(final Consumer<? super RequiredService<?>> action) {
            dependencies.forEach(action);
        }

        @Override
        public Spliterator<RequiredService<?>> spliterator() {
            return dependencies.spliterator();
        }

        int size(){
            return dependencies.size();
        }

        void clear(){
            dependencies.clear();
        }

        boolean isEmpty(){
            return dependencies.isEmpty();
        }
    }

    /**
     * Represents dependency builder.
     */
    @FunctionalInterface
    public interface SimpleDependencyBuilder{
        RequiredService<?>[] require(final Class<?>... serviceTypes);
    }

    /**
     * Returns empty or array of required services.
     * @return Empty array of required services.
     */
    protected static RequiredService<?>[] noRequiredServices(){
        return ArrayUtils.emptyArray(RequiredServiceAccessor[].class);
    }

    private static RequiredService<?>[] requiredServices(final BundleContext context, final Class<?>... dependencies) {
        return ArrayUtils.transform(dependencies, RequiredServiceAccessor.class, contract -> new SimpleDependency<>(contract, context));
    }

    /**
     * Returns builder for simple set of required services.
     * @param context Bundle context for each dependency.
     * @return Builder of required services.
     */
    protected static SimpleDependencyBuilder requiredBy(final BundleContext context){
        return serviceTypes -> requiredServices(context, serviceTypes);
    }

    /**
     * Returns builder for simple set of required services.
     * @param declaringClass Class in the bundle which declares required services.
     * @return Builder of required services.
     */
    protected static SimpleDependencyBuilder requiredBy(@Nonnull final Class<?> declaringClass){
        return requiredBy(Utils.getBundleContext(declaringClass));
    }

    private final DependencyManager bundleLevelDependencies;
    private final ActivationProperties properties;
    private final AtomicReference<ActivationState> state;
    private BundleContext context;

    /**
     * Initializes a new bundle activator in {@link com.bytex.snamp.core.AbstractBundleActivator.ActivationState#NOT_ACTIVATED} state.
     */
    protected AbstractBundleActivator(){
        bundleLevelDependencies = new DependencyManager();
        state = new AtomicReference<>(ActivationState.NOT_ACTIVATED);
        properties = new ActivationProperties();
    }

    /**
     * Gets state of this activator.
     * @return The state of this activator.
     * @see com.bytex.snamp.core.AbstractBundleActivator.ActivationState
     */
    protected final ActivationState getState(){
        return state.get();
    }

    private synchronized void serviceChanged(final BundleContext context, final ServiceEvent event) {
        if (state.get() == ActivationState.ACTIVATING)
            return;
        int resolvedDependencies = 0;
        for (final RequiredService<?> dependency : bundleLevelDependencies)
            if(dependency.processServiceEvent(event.getServiceReference(), event.getType()))
                resolvedDependencies += 1;
        switch (state.get()) {
            case ACTIVATED: //dependency lost but bundle is activated
                if (resolvedDependencies != bundleLevelDependencies.size()) {
                    state.set(ActivationState.DEACTIVATING);
                    try {
                        deactivateInternal(context, properties);
                    } catch (final Exception e) {
                        deactivationFailure(e, properties);
                    } finally {
                        state.set(ActivationState.NOT_ACTIVATED);
                    }
                }
                return;
            case NOT_ACTIVATED:    //dependencies resolved but bundle is not activated
                if (resolvedDependencies == bundleLevelDependencies.size()) {
                    state.set(ActivationState.ACTIVATING);
                    try {
                        activate(context, properties, bundleLevelDependencies);
                        state.set(ActivationState.ACTIVATED);
                    } catch (final Exception e) {
                        activationFailure(e, properties);
                        state.set(ActivationState.NOT_ACTIVATED);
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
        final BundleContext context = this.context;
        if(context != null)
        try(final LoggingScope ignored = BundleLoggingScope.processServiceChanged(this)) {
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
        this.context = context;
        try (final LoggingScope ignored = BundleLoggingScope.startBundle(this)) {
            start(context, bundleLevelDependencies);
            final DependencyListeningFilterBuilder filter = new DependencyListeningFilterBuilder();
            //try to resolve bundle-level dependencies immediately
            for (final RequiredService<?> dependency : bundleLevelDependencies) {
                filter.append(dependency);
                for (final ServiceReference<?> serviceRef : dependency.getCandidates(context))
                    serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, serviceRef));
            }
            //attach bundle-level dependencies as service listeners
            filter.applyServiceListener(context, this);
        } finally {
            bundleLevelDependencies.freeze();
        }
    }

    private void deactivateInternal(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        bundleLevelDependencies.unbind();
        deactivate(context, activationProperties);
    }

    /**
     * Stops the bundle.
     * @param context The execution context of the bundle being stopped.
     * @throws Exception An exception occurred during bundle stopping.
     */
    @Override
    public final void stop(final BundleContext context) throws Exception {
        try (final LoggingScope ignored = BundleLoggingScope.stopBundle(this)) {
            context.removeServiceListener(this);
            if (state.compareAndSet(ActivationState.ACTIVATED, ActivationState.DEACTIVATING))
                deactivateInternal(context, getActivationProperties());
            try {
                shutdown(context);
            } finally {
                //unbind all dependencies
                bundleLevelDependencies.defrost().unbind().clear();
                properties.clear();
            }
        } finally {
            state.set(ActivationState.NOT_ACTIVATED);
            this.context = null;
        }
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     * @param context The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    protected abstract void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception;

    /**
     * Activates the bundle.
     * <p>
     *     This method will be called when all bundle-level dependencies will be resolved.
     * </p>
     * @param context The execution context of the bundle being activated.
     * @param activationProperties A collection of bundle's activation properties to fill.
     * @param dependencies A set of dependencies resolved by this activator.
     * @throws Exception An exception occurred during activation.
     */
    protected abstract void activate(final BundleContext context,
                                     final ActivationPropertyPublisher activationProperties,
                                     final DependencyManager dependencies) throws Exception;

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, ActivationPropertyPublisher, DependencyManager)}  method.
     * @param e An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @MethodStub
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties){

    }

    /**
     * Handles an exception thrown by {@link } method.
     * @param e An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
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
    protected final String getFrameworkProperty(final String propertyName) {
        return Optional.ofNullable(context).map(context -> context.getProperty(propertyName)).orElse(null);
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
                return Collections.enumeration(Arrays.stream(reference.getPropertyKeys())
                        .map(reference::getProperty)
                        .collect(Collectors.toList()));
            }

            private Object get(final String key){
                return reference.getProperty(key);
            }

            @Override
            public Object get(final Object key) {
                return get(Convert.toType(key, String.class).orElseThrow(ClassCastException::new));
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
