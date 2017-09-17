package com.bytex.snamp.supervision.def;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.AbstractSupervisor;
import com.bytex.snamp.supervision.elasticity.MaxClusterSizeReachedEvent;
import com.bytex.snamp.supervision.elasticity.ScaleInEvent;
import com.bytex.snamp.supervision.elasticity.ScaleOutEvent;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.bytex.snamp.supervision.health.triggers.HealthStatusTrigger;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.JMException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents default supervisor with health check support and without elasticity management.
 * @author Roman Sakno
 * @version 2.1
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
    
    private static final class SupervisionJob extends WeakRepeater<DefaultSupervisor>{
        private final String threadName;
        private final Logger logger;
        private Duration period;

        SupervisionJob(final DefaultSupervisor input) {
            super(Duration.ZERO, input);
            threadName = "Supervision-" + input.groupName;
            logger = LoggerProvider.getLoggerForObject(this);
        }

        @Override
        public Duration getPeriod() {
            return period;
        }

        void setPeriod(final Duration value){
            period = Objects.requireNonNull(value);
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
                    logger.log(Level.SEVERE, String.format("%s is crashed", threadName), getException());
            }
        }
    }


    private final LazyReference<DefaultHealthStatusProvider> healthStatusProvider;
    private final LazyReference<DefaultElasticityManager> elasticityManager;
    private final LazyReference<DefaultResourceDiscoveryService> discoveryService;
    private final SupervisionJob updater;
    private HealthStatusTrigger userDefinedTrigger;

    public DefaultSupervisor(@Nonnull final String groupName) {
        super(groupName);
        userDefinedTrigger = HealthStatusTrigger.NO_OP;
        elasticityManager = LazyReference.strong();
        healthStatusProvider = LazyReference.strong();
        discoveryService = LazyReference.strong();
        updater = new SupervisionJob(this);
    }

    public final void setSupervisionPeriod(@Nonnull final Duration value){
        updater.setPeriod(value);
    }

    public final void setHealthStatusTrigger(@Nonnull final HealthStatusTrigger value){
        userDefinedTrigger = Objects.requireNonNull(value);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void start() {
        getDiscoveryService().setSource(this);
        super.start();
        updater.run();
    }

    @Aggregation
    protected DefaultElasticityManager getElasticityManager(){
        return elasticityManager.get(DefaultElasticityManager::new);
    }

    @Aggregation
    protected DefaultHealthStatusProvider getHealthStatusProvider() {
        return healthStatusProvider.get(DefaultHealthStatusProvider::new);
    }

    private DefaultResourceDiscoveryService createDiscoveryService(){
        return new DefaultResourceDiscoveryService(groupName);
    }

    protected DefaultResourceDiscoveryService getDiscoveryService() {
        return discoveryService.get(this, DefaultSupervisor::createDiscoveryService);
    }

    @Override
    public final void statusChanged(final ResourceGroupHealthStatus previousStatus, final ResourceGroupHealthStatus newStatus) {
        userDefinedTrigger.statusChanged(previousStatus, newStatus);
        healthStatusChanged(new DefaultHealthStatusChangedEvent(previousStatus, newStatus));
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    /**
     * Executes automatically using scheduling time.
     */
    protected void supervise() {
        getHealthStatusProvider().statusBuilder()
                .updateResourcesStatuses(getBundleContext(), getResources())
                .build(this)
                .close();
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @Override
    public final Map<String, ?> getAttributes(final String attributeName) {
        final Map<String, Object> attributes = new HashMap<>();
        for (final String resourceName : getResources()) {
            final Optional<ManagedResourceConnectorClient> clientRef = ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName);
            if (clientRef.isPresent())
                try (final ManagedResourceConnectorClient client = clientRef.get()) {
                    attributes.put(client.getManagedResourceName(), client.getAttribute(attributeName));
                } catch (final JMException e) {
                    getLogger().log(Level.WARNING, String.format("Attribute %s cannot obtained. It will be ignored in scaling.", attributeName), e);
                }
        }
        return attributes;
    }

    @Override
    @Aggregation
    public final ResourceGroupHealthStatus getHealthStatus() {
        return getHealthStatusProvider().getStatus();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void resourceRemoved(final ManagedResourceConnectorClient resourceInGroup) throws Exception {
        getHealthStatusProvider().removeResource(resourceInGroup.getManagedResourceName());
    }

    @Override
    @MethodStub
    protected void resourceAdded(final ManagedResourceConnectorClient resourceInGroup) throws Exception {
    }

    protected final void scaleIn(final Map<String, Double> policyEvaluation) {
        final ImmutableMap<String, Double> evaluation = ImmutableMap.copyOf(policyEvaluation);
        final double castingVoteWeight = getElasticityManager().getCastingVoteWeight();
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
        final double castingVoteWeight = getElasticityManager().getCastingVoteWeight();
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
        final double castingVoteWeight = getElasticityManager().getCastingVoteWeight();
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

    @Override
    public void close() throws Exception {
        super.close();
        Utils.closeAll(updater::terminate, getElasticityManager(), getHealthStatusProvider());
        elasticityManager.remove();
        healthStatusProvider.remove();
        discoveryService.remove();
    }
}
