package com.bytex.snamp.supervision.def;

import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.triggers.TriggerFactory;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.AbstractSupervisor;
import com.bytex.snamp.supervision.discovery.rest.DefaultResourceDiscoveryService;
import com.bytex.snamp.supervision.health.HealthStatusProvider;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.SupervisorInfo.HealthCheckInfo;

/**
 * Represents default supervisor with health check support and without elasticity management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultSupervisor extends AbstractSupervisor {
    private static final class HealthStatusUpdater extends WeakRepeater<DefaultSupervisor>{
        private final String threadName;

        HealthStatusUpdater(final Duration period, final DefaultSupervisor input) {
            super(period, input);
            threadName = "HealthStatusUpdater-" + input.groupName;
        }
        
        @Override
        protected String generateThreadName() {
            return threadName;
        }

        @Override
        protected void doAction() throws InterruptedException {
            getReferenceOrTerminate().updateHealthStatus();
        }

        void terminate() throws TimeoutException, InterruptedException {
            close(getPeriod());
        }
    }

    @Aggregation  //non-cached
    private DefaultHealthStatusProvider healthStatusProvider;
    @Aggregation    //non-cached
    private AttributeCheckerFactory checkerFactory;
    @Aggregation    //non-cached
    private TriggerFactory triggerFactory;
    @Aggregation
    private DefaultResourceDiscoveryService discoveryService;
    private HealthStatusUpdater updater;
    private final ConfigurationManager configurationManager;

    public DefaultSupervisor(@Nonnull final String groupName){
        super(groupName);
        this.configurationManager = null;
    }

    protected DefaultSupervisor(@Nonnull final String groupName,
                                @Nonnull final ConfigurationManager configManager){
        super(groupName);
        this.configurationManager = configManager;
    }

    protected final void setCheckerFactory(@Nonnull final AttributeCheckerFactory value){
        checkerFactory = value;
    }

    protected final void setTriggerFactory(@Nonnull final TriggerFactory value){
        triggerFactory = value;
    }

    protected final void setHealthStatusProvider(@Nonnull final DefaultHealthStatusProvider value){
        healthStatusProvider = value;
    }

    protected final void setDiscoveryService(@Nonnull final DefaultResourceDiscoveryService discoveryService){
        this.discoveryService = discoveryService;
    }

    private void updateHealthStatus() {
        final DefaultHealthStatusProvider provider = healthStatusProvider;
        if (provider == null)
            return;
        for (final String resourceName : getResources())
            ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName).ifPresent(client -> {
                final HealthStatus status;
                try {
                    status = provider.updateStatus(resourceName, client);
                } finally {
                    client.close();
                }
                getLogger().fine(String.format("Health status for resource %s is %s", resourceName, status));
            });
    }

    @Override
    protected void addResource(final String resourceName, final ManagedResourceConnector connector) {
        final DefaultResourceDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null)
            discoveryService.resourceRegistered(resourceName);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void removeResource(final String resourceName, final ManagedResourceConnector connector) {
        final DefaultHealthStatusProvider provider = healthStatusProvider;
        if (provider != null)
            provider.removeResource(resourceName);
        final DefaultResourceDiscoveryService discoveryService = this.discoveryService;
        if(discoveryService != null)
            discoveryService.resourceRemoved(resourceName);
    }

    /**
     * Stops tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     *
     * @throws Exception Unable to stop tracking resources.
     */
    @Override
    protected void stop() throws Exception {
        try {
            Utils.closeAll(updater::terminate, healthStatusProvider, discoveryService);
        } finally {
            updater = null;
            healthStatusProvider = null;
            triggerFactory = null;
            checkerFactory = null;
            discoveryService = null;
        }
    }

    protected Duration getCheckPeriod(final SupervisorInfo configuration) {
        return new DefaultSupervisorConfigurationDescriptionProvider().parseCheckPeriod(configuration);
    }

    /**
     * Initializes implementation of {@link HealthStatusProvider} provided
     * by this supervisor using supplied configuration.
     * @param healthCheckInfo Health check configuration.
     * @throws ScriptletCompilationException Unable to compile one or more scriptlets (triggers or attribute checkers).
     */
    protected final void setupHealthCheck(@Nonnull final HealthCheckInfo healthCheckInfo) throws ScriptletCompilationException {
        assert healthStatusProvider != null : "Health status provided is not initialized";
        assert triggerFactory != null : "Trigger factory is not defined";
        healthStatusProvider.setTrigger(triggerFactory.compile(healthCheckInfo.getTrigger()));
        assert checkerFactory != null : "Attribute checked factory is not defined";
        healthStatusProvider.removeCheckers();
        for (final Map.Entry<String, ? extends ScriptletConfiguration> attributeChecker : healthCheckInfo.getAttributeCheckers().entrySet())
            healthStatusProvider.addChecker(attributeChecker.getKey(), checkerFactory.compile(attributeChecker.getValue()));
    }

    /**
     * Starts the tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @param configuration Tracker startup parameters.
     * @throws Exception Unable to start tracking.
     */
    @Override
    protected void start(final SupervisorInfo configuration) throws Exception {
        setCheckerFactory(new AttributeCheckerFactory());
        setHealthStatusProvider(new DefaultHealthStatusProvider(groupName));
        setTriggerFactory(new TriggerFactory());
        setupHealthCheck(configuration.getHealthCheckConfig());
        if (configurationManager == null) {
            getLogger().warning("Configuration manager is not available. Default discovery service is disabled");
        } else {
            setDiscoveryService(new DefaultResourceDiscoveryService(groupName, configurationManager));
        }
        //start updater thread
        updater = new HealthStatusUpdater(getCheckPeriod(configuration), this);
        updater.run();
    }
}
