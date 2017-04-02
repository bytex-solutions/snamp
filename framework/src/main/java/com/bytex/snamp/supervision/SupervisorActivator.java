package com.bytex.snamp.supervision;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.internal.CMSupervisorParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.ConfigurationException;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents activator of {@link Supervisor}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class SupervisorActivator<S extends Supervisor> extends AbstractServiceLibrary {
    private static final ActivationProperty<String> SUPERVISOR_TYPE_HOLDER = defineActivationProperty(String.class, "");
    private static final ActivationProperty<CMSupervisorParser> SUPERVISOR_PARSER_HOLDER = defineActivationProperty(CMSupervisorParser.class);

    @FunctionalInterface
    protected interface SupervisorFactory<S extends Supervisor>{
        @Nonnull
        S createSupervisor(final String groupName, final DependencyManager dependencies) throws Exception;
    }

    private static final class SupervisorInstances<S extends Supervisor> extends ServiceSubRegistryManager<Supervisor ,S>{
        private final SupervisorFactory<S> factory;

        private SupervisorInstances(@Nonnull final SupervisorFactory<S> factory,
                                    final RequiredService<?>... dependencies){
            super(Supervisor.class, dependencies);
            this.factory = factory;
        }

        /**
         * Gets the base persistent identifier used as a prefix for individual dynamic services configuration.
         *
         * @return The base persistent identifier. Cannot be {@literal null} or empty string.
         */
        @Override
        protected String getFactoryPID() {
            return null;
        }

        /**
         * Updates the service with a new configuration.
         *
         * @param service       The service to update.
         * @param configuration A new configuration of the service.
         * @return The updated service.
         * @throws Exception              Unable to update service.
         * @throws ConfigurationException Invalid service configuration.
         */
        @Override
        protected S update(final S service, final Dictionary<String, ?> configuration) throws Exception {
            return null;
        }

        /**
         * Creates a new service.
         *
         * @param identity      The registration properties to fill.
         * @param configuration A new configuration of the service.
         * @return A new instance of the service.
         * @throws Exception              Unable to instantiate a new service.
         * @throws ConfigurationException Invalid configuration exception.
         */
        @Override
        protected S createService(final Map<String, Object> identity, final Dictionary<String, ?> configuration) throws Exception {
            return null;
        }

        /**
         * Releases all resources associated with the service instance.
         *
         * @param service  A service to dispose.
         * @param identity Service identity.
         * @throws Exception Unable to dispose service.
         */
        @Override
        protected void cleanupService(final S service, final Map<String, ?> identity) throws Exception {

        }
    }

    protected final String supervisorType;

    protected SupervisorActivator(final SupervisorFactory<S> factory, final RequiredService<?>... dependencies) {
        super(serviceProvider(factory, dependencies));
        supervisorType = Supervisor.getSupervisorType(getBundleContextOfObject(this).getBundle());
    }

    private static  <S extends Supervisor> ProvidedServices serviceProvider(final SupervisorFactory<S> factory, final RequiredService<?>... dependencies) {
        return (services, activationProperties, supervisorDependencies) -> {
            services.add(new SupervisorInstances<>(factory, dependencies));
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
        bundleLevelDependencies.add(ConfigurationManager.class);
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
        {
            final ConfigurationManager configurationManager = dependencies.getDependency(ConfigurationManager.class);
            assert configurationManager != null;
            final CMSupervisorParser parser = configurationManager.queryObject(CMSupervisorParser.class);
            assert parser != null : "Supervisor parser is not supported";
            activationProperties.publish(SUPERVISOR_PARSER_HOLDER, parser);
        }
        getLogger().info(String.format("Activating supervisor of type %s", supervisorType));
        super.activate(context, activationProperties, dependencies);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        super.deactivate(context, activationProperties);
        getLogger().info(String.format("Unloading supervisor of type %s", supervisorType));
    }

    /**
     * Handles an exception thrown by {@link #activate(org.osgi.framework.BundleContext, ActivationPropertyPublisher, DependencyManager)}  method.
     *
     * @param e                    An exception to handle.
     * @param activationProperties A collection of activation properties to read.
     */
    @Override
    protected void activationFailure(final Exception e, final ActivationPropertyReader activationProperties) {
        getLogger().log(Level.SEVERE, String.format("Unable to activate %s supervisor",
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
        getLogger().log(Level.SEVERE, String.format("Unable to deactivate %s supervisor instance",
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
