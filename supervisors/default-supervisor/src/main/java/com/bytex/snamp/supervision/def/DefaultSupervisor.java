package com.bytex.snamp.supervision.def;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.AbstractSupervisor;
import com.bytex.snamp.supervision.elasticity.MaxClusterSizeReachedEvent;
import com.bytex.snamp.supervision.elasticity.ScaleInEvent;
import com.bytex.snamp.supervision.elasticity.ScaleOutEvent;
import com.bytex.snamp.supervision.elasticity.policies.InvalidScalingPolicyException;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyFactory;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.bytex.snamp.supervision.health.triggers.HealthStatusTrigger;
import com.bytex.snamp.supervision.health.triggers.TriggerFactory;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
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
public class DefaultSupervisor extends AbstractSupervisor implements HealthStatusTrigger, ScalingPolicyEvaluationContext {

    private final class DefaultHealthStatusChangedEvent extends HealthStatusChangedEvent {
        private static final long serialVersionUID = -6608026114593286031L;
        private final ResourceGroupHealthStatus previousStatus;
        private final ResourceGroupHealthStatus newStatus;

        private DefaultHealthStatusChangedEvent(@Nonnull final ResourceGroupHealthStatus newStatus,
                                                @Nonnull final ResourceGroupHealthStatus previousStatus) {
            super(DefaultSupervisor.this, DefaultSupervisor.this.groupName);
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
        }

        @Override
        public DefaultSupervisor getSource() {
            return DefaultSupervisor.this;
        }

        @Override
        public ResourceGroupHealthStatus getNewStatus() {
            return newStatus;
        }

        @Override
        public ResourceGroupHealthStatus getPreviousStatus() {
            return previousStatus;
        }
    }
    
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
            close(getPeriod().multipliedBy(2L));
        }

        /**
         * Clears a weak reference to the object participated in processing.
         *
         * @param s A new repeater state.
         */
        @Override
        protected void stateChanged(final RepeaterState s) {
            super.stateChanged(s);
            switch (s){
                case FAILED:
                    logger.log(Level.SEVERE, String.format("%s is crushed", threadName), getException());
            }
        }
    }

    @Aggregation  //non-cached
    private DefaultHealthStatusProvider healthStatusProvider;
    private AttributeCheckerFactory checkerFactory;
    private TriggerFactory triggerFactory;
    private ScalingPolicyFactory policyFactory;
    @Aggregation
    private DefaultResourceDiscoveryService discoveryService;
    private SupervisorRepeater updater;
    private HealthStatusTrigger userDefinedTrigger;
    @Aggregation
    private DefaultElasticityManager elasticityManager;

    /**
     * Gets a reference to the current member in SNAMP cluster.
     */
    protected final ClusterMember clusterMember;

    public DefaultSupervisor(@Nonnull final String groupName){
        super(groupName);
        clusterMember = ClusterMember.get(getBundleContext());
    }

    protected final void overrideElasticityManager(@Nonnull final DefaultElasticityManager value){
        elasticityManager = Objects.requireNonNull(value);
    }

    protected final void overrideScalingPolicyFactory(@Nonnull final ScalingPolicyFactory value){
        policyFactory = Objects.requireNonNull(value);
    }

    private void setupScalingPolicyFactory(){
        if(policyFactory == null)
            overrideScalingPolicyFactory(new ScalingPolicyFactory());
        else
            getLogger().fine(String.format("ScalingPolicyFactory is overridden with %s", policyFactory));
    }

    protected final void overrideCheckerFactory(@Nonnull final AttributeCheckerFactory value){
        checkerFactory = Objects.requireNonNull(value);
    }

    private void setupCheckerFactory(){
        if(checkerFactory == null)
            overrideCheckerFactory(new AttributeCheckerFactory());
        else
            getLogger().fine(String.format("AttributeCheckerFactory is overridden with %s", checkerFactory));
    }

    protected final void overrideTriggerFactory(@Nonnull final TriggerFactory value){
        triggerFactory = Objects.requireNonNull(value);
    }

    private void setupTriggerFactory(){
        if(triggerFactory == null)
            overrideTriggerFactory(new TriggerFactory());
        else
            getLogger().fine(String.format("TriggerFactory is overridden with %s", triggerFactory));
    }

    protected final void overrideHealthStatusProvider(@Nonnull final DefaultHealthStatusProvider value){
        healthStatusProvider = Objects.requireNonNull(value);
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

    @Override
    public final void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus) {
        final HealthStatusTrigger userDefinedTrigger = this.userDefinedTrigger;
        if (userDefinedTrigger != null)
            userDefinedTrigger.statusChanged(previousStatus, newStatus);
        healthStatusChanged(new DefaultHealthStatusChangedEvent(previousStatus, newStatus));
    }

    private void updateHealthStatus(final DefaultHealthStatusProvider provider) {
        provider.statusBuilder()
                .updateResourcesStatuses(getBundleContext(), getResources())
                .build(this)
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
            Utils.closeAll(updater::terminate, healthStatusProvider, elasticityManager);
        } finally {
            updater = null;
            healthStatusProvider = null;
            triggerFactory = null;
            checkerFactory = null;
            discoveryService = null;
            policyFactory = null;
            elasticityManager = null;
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
        userDefinedTrigger = triggerFactory.compile(healthCheckInfo.getTrigger());
        assert checkerFactory != null : "Attribute checked factory is not defined";
        healthStatusProvider.removeCheckers();
        final EntryReader<String, ScriptletConfiguration, InvalidAttributeCheckerException> walker = (attributeName, checker) -> {
            healthStatusProvider.addChecker(attributeName, checkerFactory.compile(checker));
            return true;
        };
        walker.walk(healthCheckInfo.getAttributeCheckers());
    }

    private void setupScaling(@Nonnull final SupervisorInfo.AutoScalingInfo scalingConfig) throws InvalidScalingPolicyException {
        if (scalingConfig.isEnabled() && elasticityManager != null) {
            assert policyFactory != null;
            elasticityManager.setCooldownTime(scalingConfig.getCooldownTime());
            elasticityManager.setMaxClusterSize(scalingConfig.getMaxClusterSize());
            elasticityManager.setMinClusterSize(scalingConfig.getMinClusterSize());
            elasticityManager.setScalingSize(scalingConfig.getScalingSize());
            final EntryReader<String, ScriptletConfiguration, InvalidScalingPolicyException> walker = (policyName, policy) -> {
                elasticityManager.addScalingPolicy(policyName, policyFactory.compile(policy));
                return true;
            };
            walker.walk(scalingConfig.getPolicies());
        }
    }

    protected final void scaleIn(final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        final double castingVoteWeight = elasticityManager.getCastingVoteWeight();
        scalingHappens(new ScaleInEvent(this, groupName) {
            private static final long serialVersionUID = -7114648391882166130L;

            @Override
            public double getCastingVoteWeight() {
                return castingVoteWeight;
            }

            @Override
            public DefaultSupervisor getSource() {
                return DefaultSupervisor.this;
            }

            @Override
            public ImmutableMap<String, Double> getPolicyEvaluationResult() {
                return evaluation;
            }
        });
    }

    protected final void scaleOut(final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        final double castingVoteWeight = elasticityManager.getCastingVoteWeight();
        scalingHappens(new ScaleOutEvent(this, groupName) {
            private static final long serialVersionUID = 3268384740398039079L;

            @Override
            public double getCastingVoteWeight() {
                return castingVoteWeight;
            }

            @Override
            public DefaultSupervisor getSource() {
                return DefaultSupervisor.this;
            }

            @Override
            public ImmutableMap<String, Double> getPolicyEvaluationResult() {
                return evaluation;
            }
        });
    }

    protected final void maxClusterSizeReached(final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        final double castingVoteWeight = elasticityManager.getCastingVoteWeight();
        scalingHappens(new MaxClusterSizeReachedEvent(this, groupName) {
            private static final long serialVersionUID = 4539949257630314963L;

            @Override
            public double getCastingVoteWeight() {
                return castingVoteWeight;
            }

            @Override
            public DefaultSupervisor getSource() {
                return DefaultSupervisor.this;
            }

            @Override
            public Map<String, Double> getPolicyEvaluationResult() {
                return evaluation;
            }
        });
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
        setupScalingPolicyFactory();
        setupScaling(configuration.getAutoScalingConfig());
        //start updater thread
        updater = new SupervisorRepeater(parser.parseCheckPeriod(configuration), this);
        updater.run();
    }
}
