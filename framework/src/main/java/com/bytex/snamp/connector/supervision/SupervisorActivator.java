package com.bytex.snamp.connector.supervision;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.configuration.internal.CMManagedResourceGroupParser;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.LoggerProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents activator of {@link ManagedResourceGroupSupervisor}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class SupervisorActivator<TSupervisor extends ManagedResourceGroupSupervisor> extends AbstractServiceLibrary {
    private static final String CATEGORY = "supervisor";
    private static final String GROUP_NAME_PROPERTY = "groupName";
    private static final ActivationProperty<CMManagedResourceGroupParser> GROUP_PARSER_HOLDER = defineActivationProperty(CMManagedResourceGroupParser.class);
    private static final ActivationProperty<String> SUPERVISOR_TYPE_HOLDER = defineActivationProperty(String.class);

    protected interface ManagedResourceGroupSupervisorFactory<TSupervisor extends ManagedResourceGroupSupervisor>{
        TSupervisor createSupervisor(final String groupName,
                                     final ManagedResourceGroupConfiguration configuration,
                                     final DependencyManager dependencies) throws Exception;
    }


    private static final class SupervisorRegistry<TSupervisor extends ManagedResourceGroupSupervisor> extends ServiceSubRegistryManager<ManagedResourceGroupSupervisor, TSupervisor>{
        private final String supervisorType;
        private final ManagedResourceGroupSupervisorFactory<TSupervisor> factory;

        private SupervisorRegistry(final String supervisorType,
                                                 final ManagedResourceGroupSupervisorFactory<TSupervisor> factory,
                                                 final RequiredService<?>... dependencies){
            super(ManagedResourceGroupSupervisor.class, dependencies);
            this.dependencies.add(ConfigurationManager.class);
            this.supervisorType = Objects.requireNonNull(supervisorType);
            this.factory = Objects.requireNonNull(factory);
        }

        SupervisorRegistry(final ManagedResourceGroupSupervisorFactory<TSupervisor> factory,
                           final RequiredService<?>... dependencies) {
            this(ManagedResourceGroupSupervisor.getSupervisorType(getBundleContextOfObject(factory).getBundle()), factory, dependencies);
        }

        private CMManagedResourceGroupParser getParser(){
            return getActivationPropertyValue(GROUP_PARSER_HOLDER);
        }
        
        @Override
        protected String getFactoryPID() {
            return getParser().getFactoryPersistentID(supervisorType);
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
        protected TSupervisor update(final TSupervisor service, final Dictionary<String, ?> configuration) throws Exception {
            return null;
        }

        private TSupervisor createService(final Map<String, Object> identity,
                                          final String groupName,
                                          final ManagedResourceGroupConfiguration configuration) throws Exception{
            identity.put(ManagedResourceGroupSupervisor.CATEGORY_PROPERTY, CATEGORY);
            identity.put(ManagedResourceGroupSupervisor.TYPE_CAPABILITY_ATTRIBUTE, supervisorType);
            identity.put(GROUP_NAME_PROPERTY, groupName);
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
        protected TSupervisor createService(final Map<String, Object> identity, final Dictionary<String, ?> configuration) throws Exception {
            final String groupName = getParser().getGroupName(configuration);
            final ManagedResourceGroupConfiguration newConfig = getParser().parse(configuration).getValue();
            if(newConfig == null)
                throw new IllegalStateException(String.format("Supervisor %s cannot be created. Configuration not found.", supervisorType));
            newConfig.expandParameters();
            return createService(identity, groupName, newConfig);
        }

        @Override
        protected void cleanupService(final TSupervisor service, final Map<String, ?> identity) throws Exception {
            service.close();
        }
    }

    protected final String supervisorType;

    protected SupervisorActivator() {
        supervisorType = ManagedResourceGroupSupervisor.getSupervisorType(getBundleContextOfObject(this).getBundle());
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
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
            final CMManagedResourceGroupParser parser = configurationManager.queryObject(CMManagedResourceGroupParser.class);
            assert parser != null : "CMManagedResourceGroupParser is not supported";
            activationProperties.publish(GROUP_PARSER_HOLDER, parser);
        }
        getLogger().info(String.format("Activating supervisor of type %s", supervisorType));
        super.activate(context, activationProperties, dependencies);
    }

    /**
     * Deactivates this library.
     * <p>
     * This method will be invoked when at least one dependency was lost.
     * </p>
     *
     * @param context              The execution context of the library being deactivated.
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Deactivation error.
     */
    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        super.deactivate(context, activationProperties);
        getLogger().info(String.format("Unloading supervisor of type %s", supervisorType));
    }
}
