package com.bytex.snamp.supervision;

import com.bytex.snamp.Internal;
import com.bytex.snamp.SingletonMap;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.internal.CMSupervisorParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents activator of {@link Supervisor}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class SupervisorActivator<S extends Supervisor> extends AbstractServiceLibrary {
    private static final ActivationProperty<String> SUPERVISOR_TYPE_HOLDER = defineActivationProperty(String.class, "");
    private static final ActivationProperty<CMSupervisorParser> SUPERVISOR_PARSER_HOLDER = defineActivationProperty(CMSupervisorParser.class);
    private static final ActivationProperty<Logger> LOGGER_HOLDER = defineActivationProperty(Logger.class, Logger.getAnonymousLogger());

    /**
     * Represents lifecycle manager of supervisor.
     * @param <S> Type of supervisor implementation.
     * @since 2.1
     */
    protected static abstract class SupervisorLifecycleManager<S extends Supervisor> extends ServiceSubRegistryManager<Supervisor ,S> {
        private final LazyReference<Logger> logger = LazyReference.strong();
        private final ThreadLocal<String> groupName = new ThreadLocal<>();
        private final Map<String, SupervisorConfiguration> supervisors = new HashMap<>();

        protected SupervisorLifecycleManager(final RequiredService<?>... dependencies) {
            super(Supervisor.class, dependencies);
        }

        protected SupervisorLifecycleManager(final Iterable<Class<? super S>> interfaces,
                                             final RequiredService<?>... dependencies) {
            super(Supervisor.class, interfaces, dependencies);
        }

        private Logger getLoggerImpl() {
            return getActivationPropertyValue(LOGGER_HOLDER);
        }

        /**
         * Gets logger associated with this manager.
         *
         * @return Logger.
         */
        @Override
        protected final Logger getLogger() {
            return logger.get(this, SupervisorLifecycleManager::getLoggerImpl);
        }

        private String getSupervisorType() {
            return getActivationPropertyValue(SUPERVISOR_TYPE_HOLDER);
        }

        private CMSupervisorParser getParser() {
            return getActivationPropertyValue(SUPERVISOR_PARSER_HOLDER);
        }

        @Override
        @Internal
        protected final String getFactoryPID() {
            return getParser().getFactoryPersistentID(getSupervisorType());
        }

        private SingletonMap<String, ? extends SupervisorConfiguration> parseConfig(final Dictionary<String, ?> configuration) throws IOException {
            final SingletonMap<String, ? extends SupervisorConfiguration> newConfig = getParser().parse(configuration);
            newConfig.getValue().setType(getSupervisorType());
            newConfig.getValue().expandParameters();
            return newConfig;
        }

        /**
         * Updates existing supervisor instance with new configuration.
         *
         * @param supervisor    Existing supervisor instance.
         * @param configuration A new configuration.
         * @return Updated instance of supervisor or newly created.
         * @throws Exception Unable to update supervisor.
         */
        @Nonnull
        protected S updateSupervisor(@Nonnull final S supervisor,
                                     @Nonnull final SupervisorConfiguration configuration) throws Exception {
            final String groupName = this.groupName.get();
            assert !isNullOrEmpty(groupName);
            final SupervisorConfiguration existingConfig = supervisors.get(groupName);
            return Objects.equals(configuration, existingConfig) ? supervisor : createSupervisor(groupName, configuration);
        }

        @Override
        protected final S updateService(S supervisor, final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends SupervisorConfiguration> newConfig = parseConfig(configuration);
            groupName.set(newConfig.getKey());
            try {
                supervisor = updateSupervisor(supervisor, newConfig.getValue());
            } finally {
                groupName.remove();
            }
            getLogger().info(String.format("Supervisor %s is updated", supervisor));
            return supervisor;
        }

        @Nonnull
        protected abstract S createSupervisor(@Nonnull final String groupName,
                                              @Nonnull final SupervisorConfiguration configuration) throws Exception;

        @Override
        protected final S activateService(final ServiceIdentityBuilder identity, final Dictionary<String, ?> configuration) throws Exception {
            final SingletonMap<String, ? extends SupervisorConfiguration> newConfig = parseConfig(configuration);
            final S supervisor = createSupervisor(newConfig.getKey(), newConfig.getValue());
            identity.acceptAll(new SupervisorSelector(newConfig.getValue()).setGroupName(newConfig.getKey()));
            supervisors.put(newConfig.getKey(), newConfig.getValue());
            getLogger().info(String.format("Supervisor %s is instantiated", supervisor));
            return supervisor;
        }

        @Override
        protected final void disposeService(final S supervisor, final Map<String, ?> identity) throws Exception {
            final String groupName = SupervisorSelector.getGroupName(identity);
            supervisors.remove(groupName);
            supervisor.close();
            getLogger().info(String.format("Supervisor %s is destroyed", supervisor));
        }

        @Override
        protected final void failedToUpdateService(final Logger logger,
                                                   final String servicePID,
                                                   final Dictionary<String, ?> configuration,
                                                   final Exception e) {
            logger.log(Level.SEVERE,
                    String.format("Unable to update supervisor. Type: %s, instance: %s",
                            getSupervisorType(),
                            servicePID),
                    e);
        }

        @Override
        protected final void failedToCleanupService(final Logger logger,
                                                    final String servicePID,
                                                    final Exception e) {
            logger.log(Level.SEVERE, String.format("Unable to release gateway. Type: %s, instance: %s", getSupervisorType(), servicePID),
                    e);
        }
    }

    /**
     * Represents superclass for all optional supervisor-related service factories.
     * You cannot derive from this class directly.
     * @param <T> Type of the supervisor-related service implementation.
     * @author Roman Sakno
     * @since 2.0
     * @version 2.1
     * @see #configurationDescriptor(Supplier)
     */
    protected final static class SupportServiceManager<T extends SupportService> extends ProvidedService<T>{
        private final Function<DependencyManager, T> activator;

        private SupportServiceManager(final Class<? super T> contract,
                                      final Function<DependencyManager, T> activator,
                                      final RequiredService<?>... dependencies) {
            super(contract, dependencies);
            this.activator = Objects.requireNonNull(activator);
        }

        @Override
        @Nonnull
        protected T activateService(final ServiceIdentityBuilder identity) throws Exception {
            identity.acceptAll(new SupervisorSelector().setSupervisorType(getSupervisorType()));
            return activator.apply(dependencies);
        }

        private String getSupervisorType(){
            return getActivationPropertyValue(SUPERVISOR_TYPE_HOLDER);
        }
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<T> configurationDescriptor(final Function<DependencyManager, T> factory, final RequiredService<?>... dependencies) {
        return new SupportServiceManager<>(ConfigurationEntityDescriptionProvider.class, factory, dependencies);
    }

    protected static <T extends ConfigurationEntityDescriptionProvider> SupportServiceManager<T> configurationDescriptor(final Supplier<T> factory) {
        return configurationDescriptor(dependencies -> factory.get());
    }

    /**
     * Type of this supervisor.
     */
    protected final String supervisorType;
    private final Logger logger;

    protected SupervisorActivator(final SupervisorLifecycleManager<S> factory,
                                  final SupportServiceManager<?>... optionalServices) {
        this(factory, emptyArray(RequiredService[].class), optionalServices);
    }

    protected SupervisorActivator(final SupervisorLifecycleManager<S> factory,
                                  final RequiredService<?>[] dependencies,
                                  final SupportServiceManager<?>[] optionalServices){
        super(serviceProvider(factory, dependencies, optionalServices));
        supervisorType = Supervisor.getSupervisorType(getBundleContextOfObject(this).getBundle());
        logger = LoggerProvider.getLoggerForObject(this);
    }

    private static  <S extends Supervisor> ProvidedServices serviceProvider(final SupervisorLifecycleManager<S> factory,
                                                                            final RequiredService<?>[] dependencies,
                                                                            final SupportServiceManager<?>[] optionalServices) {
        return (services, activationProperties, supervisorDependencies) -> {
            services.add(factory);
            Collections.addAll(services, optionalServices);
        };
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(ConfigurationManager.class, context);
    }

    /**
     * Registers all services in this library.
     *
     * @param context              The execution context of the library being activated.
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         Dependencies resolved by this activator.
     * @throws Exception Bundle activation error.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final DependencyManager dependencies) throws Exception {
        activationProperties.publish(SUPERVISOR_TYPE_HOLDER, supervisorType);
        activationProperties.publish(LOGGER_HOLDER, logger);
        {
            final CMSupervisorParser parser = dependencies.getService(ConfigurationManager.class)
                    .queryObject(CMSupervisorParser.class)
                    .orElseThrow(AssertionError::new);
            activationProperties.publish(SUPERVISOR_PARSER_HOLDER, parser);
        }
        logger.info(String.format("Activating supervisor of type %s", supervisorType));
        super.activate(context, activationProperties, dependencies);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        super.deactivate(context, activationProperties);
        logger.info(String.format("Unloading supervisor of type %s", supervisorType));
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, ActivationPropertyPublisher, DependencyManager)}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        logger.log(Level.SEVERE, String.format("Unable to activate %s supervisor",
                supervisorType),
                e);
    }

    /**
     * Handles an exception thrown by {@link } method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void deactivationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        logger.log(Level.SEVERE, String.format("Unable to deactivate %s supervisor instance",
                supervisorType),
                e);
    }

    private static List<Bundle> getSupervisorBundles(final BundleContext context){
        return Arrays.stream(context.getBundles())
                .filter(Supervisor::isSupervisorBundle)
                .collect(Collectors.toList());
    }

    static List<Bundle> getSupervisorBundles(final BundleContext context, final String supervisorType) {
        return Arrays.stream(context.getBundles())
                .filter(bnd -> Supervisor.getSupervisorType(bnd).equals(supervisorType))
                .collect(Collectors.toList());
    }

    /**
     * Disables all supervisors loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @return Number of stopped bundles.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop supervisors.
     */
    public static int disableSupervisors(final BundleContext context) throws BundleException {
        if(context == null) throw new IllegalStateException("context is null.");
        int count = 0;
        for(final Bundle bnd: getSupervisorBundles(context)) {
            bnd.stop();
            count += 1;
        }
        return count;
    }

    /**
     * Disables supervisor by its type.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param supervisorType The type of supervisor to disable.
     * @return {@literal true}, if bundle with the specified supervisor exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to stop supervisor.
     */
    public static boolean disableSupervsior(final BundleContext context, final String supervisorType) throws BundleException {
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getSupervisorBundles(context, supervisorType)) {
            bnd.stop();
            success = true;
        }
        return success;
    }

    /**
     * Enables all supervisors loaded into the current OSGi environment.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @return Number of started bundles with supervisors.
     * @throws BundleException Unable to start supervisors.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     */
    public static int enableSupervisors(final BundleContext context) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        int count = 0;
        for(final Bundle bnd: getSupervisorBundles(context)) {
            bnd.start();
            count += 1;
        }
        return count;
    }

    /**
     * Enables the specified supervisor.
     * @param context The context of the calling bundle. Cannot be {@literal null}.
     * @param supervisorType The type of supervisor to enable.
     * @return {@literal true}, if bundle with the specified supervisor exists; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException context is {@literal null}.
     * @throws BundleException Unable to start bundle with supervisor.
     */
    public static boolean enableSupervisor(final BundleContext context, final String supervisorType) throws BundleException{
        if(context == null) throw new IllegalArgumentException("context is null.");
        boolean success = false;
        for(final Bundle bnd: getSupervisorBundles(context, supervisorType)) {
            bnd.start();
            success = true;
        }
        return success;
    }

    /**
     * Gets a collection of installed gateways (types).
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     * @return A collection of installed gateways.
     */
    public static Collection<String> getInstalledSupervisors(final BundleContext context) {
        final Collection<Bundle> candidates = getSupervisorBundles(context);
        return candidates.stream()
                .map(Supervisor::getSupervisorType)
                .filter(name -> !isNullOrEmpty(name))
                .collect(Collectors.toSet());
    }
}
