package com.bytex.snamp.supervision.def;

import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.health.triggers.TriggerFactory;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.AbstractSupervisor;
import com.bytex.snamp.supervision.health.HealthStatusProvider;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.SupervisorInfo.HealthCheckInfo;

/**
 * Represents default supervisor with health check support and without elasticity management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultSupervisor extends AbstractSupervisor {
    private static final class SupervisorRepeater extends WeakRepeater<DefaultSupervisor>{
        private final String threadName;
        private final Logger logger;

        SupervisorRepeater(final Duration period, final DefaultSupervisor input) {
            super(period, input);
            threadName = "Supervision-" + input.groupName;
            this.logger = input.getLogger();
        }
        
        @Override
        protected String generateThreadName() {
            return threadName;
        }

        @Override
        protected void doAction() throws InterruptedException {
            getReferenceOrTerminate().supervise();
        }

        void terminate() throws TimeoutException, InterruptedException {
            close(getPeriod());
        }

        /**
         * Clears a weak reference to the object participated in processing.
         *
         * @param s A new repeater state.
         */
        @Override
        protected void stateChanged(final RepeaterState s) {
            switch (s){
                case FAILED:
                    logger.log(Level.SEVERE, String.format("%s is crushed", threadName), getException());
            }
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
    private SupervisorRepeater updater;
    /**
     * Gets a reference to the current member in SNAMP cluster.
     */
    protected final ClusterMember clusterMember;

    public DefaultSupervisor(@Nonnull final String groupName){
        super(groupName);
        clusterMember = ClusterMember.get(getBundleContext());
    }

    protected final void overrideCheckerFactory(@Nonnull final AttributeCheckerFactory value){
        checkerFactory = value;
    }

    private void setupCheckerFactory(){
        if(checkerFactory == null)
            overrideCheckerFactory(new AttributeCheckerFactory());
        else
            getLogger().fine(String.format("AttributeCheckerFactory is overridden with %s", checkerFactory));
    }

    protected final void overrideTriggerFactory(@Nonnull final TriggerFactory value){
        triggerFactory = value;
    }

    private void setupTriggerFactory(){
        if(triggerFactory == null)
            overrideTriggerFactory(new TriggerFactory());
        else
            getLogger().fine(String.format("TriggerFactory is overridden with %s", triggerFactory));
    }

    protected final void overrideHealthStatusProvider(@Nonnull final DefaultHealthStatusProvider value){
        healthStatusProvider = value;
    }

    private void setupHealthStatusProvider(){
        if(healthStatusProvider == null)
            overrideHealthStatusProvider(new DefaultHealthStatusProvider());
        else
            getLogger().fine(String.format("HealthStatusProvider is overridden with %s", healthStatusProvider));
    }

    protected final void overrideDiscoveryService(@Nonnull final DefaultResourceDiscoveryService value){
        value.setSource(this);
        discoveryService = value;
    }

    private void setupDiscoveryService(){
        if(discoveryService == null)
            overrideDiscoveryService(new DefaultResourceDiscoveryService(groupName));
        else
            getLogger().fine(String.format("ResourceDiscoveryService is overridden with %s", discoveryService));
    }

    private void updateHealthStatus(final DefaultHealthStatusProvider provider) {
        provider.statusBuilder()
                .updateResourcesStatuses(getBundleContext(), getResources())
                .build()
                .close();
    }

    /**
     * Executes automatically using scheduling time.
     */
    protected void supervise() {
        queryObject(DefaultHealthStatusProvider.class).ifPresent(this::updateHealthStatus);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void removeResource(final String resourceName, final ManagedResourceConnector connector) {
        final DefaultHealthStatusProvider provider = healthStatusProvider;
        if (provider != null)
            provider.removeResource(resourceName);
        super.removeResource(resourceName, connector);
    }

    /**
     * Stops tracking resources.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     *
     * @throws Exception Unable to stop tracking resources.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void stop() throws Exception {
        try {
            Utils.closeAll(updater::terminate, healthStatusProvider);
        } finally {
            updater = null;
            healthStatusProvider = null;
            triggerFactory = null;
            checkerFactory = null;
            discoveryService = null;
        }
    }

    /**
     * Initializes implementation of {@link HealthStatusProvider} provided
     * by this supervisor using supplied configuration.
     * @param healthCheckInfo Health check configuration.
     * @throws ScriptletCompilationException Unable to compile one or more scriptlets (triggers or attribute checkers).
     */
    private void setupHealthCheck(@Nonnull final HealthCheckInfo healthCheckInfo) throws ScriptletCompilationException {
        assert healthStatusProvider != null : "Health status provided is not initialized";
        assert triggerFactory != null : "Trigger factory is not defined";
        healthStatusProvider.setTrigger(triggerFactory.compile(healthCheckInfo.getTrigger()));
        assert checkerFactory != null : "Attribute checked factory is not defined";
        healthStatusProvider.removeCheckers();
        for (final Map.Entry<String, ? extends ScriptletConfiguration> attributeChecker : healthCheckInfo.getAttributeCheckers().entrySet())
            healthStatusProvider.addChecker(attributeChecker.getKey(), checkerFactory.compile(attributeChecker.getValue()));
    }

    protected DefaultSupervisorConfigurationDescriptionProvider getDescriptionProvider(){
        return new DefaultSupervisorConfigurationDescriptionProvider();
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
    @OverridingMethodsMustInvokeSuper
    protected void start(final SupervisorInfo configuration) throws Exception {
        final DefaultSupervisorConfigurationDescriptionProvider parser = getDescriptionProvider();
        setupCheckerFactory();
        setupTriggerFactory();
        setupHealthStatusProvider();
        setupDiscoveryService();
        setupHealthCheck(configuration.getHealthCheckConfig());
        //start updater thread
        updater = new SupervisorRepeater(parser.parseCheckPeriod(configuration), this);
        updater.run();
    }
}
