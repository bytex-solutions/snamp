package com.bytex.snamp.supervision.def;

import com.bytex.snamp.*;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.connector.attributes.checkers.AttributeCheckerFactory;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.core.ScriptletCompilationException;
import com.bytex.snamp.supervision.SupervisorActivator;
import com.bytex.snamp.supervision.elasticity.policies.InvalidScalingPolicyException;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyFactory;
import com.bytex.snamp.supervision.health.triggers.TriggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Represents activator for {@link DefaultSupervisor}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class DefaultSupervisorActivator<S extends DefaultSupervisor> extends SupervisorActivator<S> {

    /**
     * Provides lifecycle management for custom supervisor based on {@link DefaultSupervisor}.
     * @param <S> Type of supervisor implementation.
     * @since 2.1
     */
    protected static abstract class DefaultSupervisorLifecycleManager<S extends DefaultSupervisor> extends SupervisorLifecycleManager<S>{
        private final LazyReference<ScalingPolicyFactory> policyFactory = LazyReference.soft();
        private final LazyReference<TriggerFactory> triggerFactory = LazyReference.soft();
        private final LazyReference<AttributeCheckerFactory> checkerFactory = LazyReference.soft();

        protected DefaultSupervisorLifecycleManager(final RequiredService<?>... dependencies) {
            super(dependencies);
        }

        protected DefaultSupervisorLifecycleManager(final Iterable<Class<? super S>> interfaces, final RequiredService<?>... dependencies) {
            super(interfaces, dependencies);
        }

        protected DefaultSupervisorConfigurationDescriptionProvider getDescriptionProvider(){
            return new DefaultSupervisorConfigurationDescriptionProvider();
        }

        protected ScalingPolicyFactory createScalingPolicyFactory(){
            return new ScalingPolicyFactory();
        }

        protected TriggerFactory createTriggerFactory(){
            return new TriggerFactory();
        }

        protected AttributeCheckerFactory createAttributeCheckerFactory(){
            return new AttributeCheckerFactory();
        }

        /**
         * Creates a new instance of configured supervisor.
         * @param groupName Supervised group name.
         * @param configuration Configuration of supervisor.
         * @return A new instance of configured supervisor.
         * @throws Exception Unable to instantiate supervisor.
         */
        @Nonnull
        protected abstract S createSupervisor(@Nonnull final String groupName,
                                              @Nonnull final Map<String, String> configuration) throws Exception;

        protected void configureElasticity(final S supervisor,
                                         final SupervisorInfo.AutoScalingInfo configuration) throws InvalidScalingPolicyException {
            final DefaultElasticityManager manager = supervisor.getElasticityManager();
            final ScalingPolicyFactory policyFactory = this.policyFactory.get(this, DefaultSupervisorLifecycleManager::createScalingPolicyFactory);
            manager.setCooldownTime(configuration.getCooldownTime());
            manager.setMaxClusterSize(configuration.getMaxClusterSize());
            manager.setMinClusterSize(configuration.getMinClusterSize());
            manager.setScalingSize(configuration.getScalingSize());
            final EntryReader<String, ScriptletConfiguration, InvalidScalingPolicyException> walker = (policyName, policy) -> {
                manager.addScalingPolicy(policyName, policyFactory.compile(policy));
                return true;
            };
            walker.walk(configuration.getPolicies());
        }

        protected void configureHealthChecks(final S supervisor,
                                           final SupervisorInfo.HealthCheckInfo configuration) throws ScriptletCompilationException {
            final TriggerFactory triggerFactory = this.triggerFactory.get(this, DefaultSupervisorLifecycleManager::createTriggerFactory);
            supervisor.setHealthStatusTrigger(triggerFactory.compile(configuration.getTrigger()));
            final AttributeCheckerFactory checkerFactory = this.checkerFactory.get(this, DefaultSupervisorLifecycleManager::createAttributeCheckerFactory);
            final DefaultHealthStatusProvider hsProvider = supervisor.getHealthStatusProvider();
            hsProvider.removeCheckers();
            final EntryReader<String, ScriptletConfiguration, InvalidAttributeCheckerException> walker = (attributeName, checker) -> {
                hsProvider.addChecker(attributeName, checkerFactory.compile(checker));
                return true;
            };
            walker.walk(configuration.getAttributeCheckers());
        }

        @MethodStub
        protected void configureDiscovery(final S supervisor,
                                          final SupervisorInfo.ResourceDiscoveryInfo configuration){
            
        }

        @Nonnull
        @Override
        protected final S createSupervisor(@Nonnull final String groupName, @Nonnull final SupervisorInfo configuration) throws Exception {
            final S supervisor = createSupervisor(groupName, (Map<String, String>) configuration);
            configureElasticity(supervisor, configuration.getAutoScalingConfig());
            configureHealthChecks(supervisor, configuration.getHealthCheckConfig());
            supervisor.setSupervisionPeriod(getDescriptionProvider().parseCheckPeriod(configuration));
            supervisor.start();
            return supervisor;
        }
    }

    private static final class GroovySupervisorLifecycleManager extends DefaultSupervisorLifecycleManager {
        private static final String GROOVY_MANAGER_PATH = "groovyElasticityManager";

        @Nonnull
        @Override
        protected DefaultSupervisor createSupervisor(@Nonnull final String groupName, @Nonnull final Map configuration) throws Exception {
            final DefaultSupervisor result;
            if (configuration.containsKey(GROOVY_MANAGER_PATH)) {
                final String scriptPath = Objects.toString(configuration.get(GROOVY_MANAGER_PATH));
                @SuppressWarnings("unchecked")
                final Properties environment = MapUtils.toProperties(configuration);
                result = new GroovySupervisor(groupName, scriptPath, environment);
            } else
                result = new DefaultSupervisor(groupName);
            return result;
        }
    }

    /**
     * Activates default implementation of supervisor.
     * @deprecated This constructor should not be used directly from your code.
     */
    @SuppressWarnings("unchecked")
    @SpecialUse(SpecialUse.Case.OSGi)
    @Internal
    @Deprecated
    public DefaultSupervisorActivator() {
        super(new GroovySupervisorLifecycleManager());
    }

    protected DefaultSupervisorActivator(final DefaultSupervisorLifecycleManager<S> factory,
                                         final SupportServiceManager<?>... optionalServices) {
        super(factory, optionalServices);
    }

    protected DefaultSupervisorActivator(final DefaultSupervisorLifecycleManager<S> factory,
                                         final RequiredService<?>[] dependencies,
                                         final SupportServiceManager<?>[] optionalServices) {
        super(factory, dependencies, optionalServices);
    }
}
