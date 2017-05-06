package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;
import com.bytex.snamp.moa.ReduceOperation;
import com.google.common.collect.Range;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Represents configuration of the supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SupervisorConfiguration extends TypedEntityConfiguration, SupervisorInfo {
    /**
     * Represents default supervisor type.
     */
    String DEFAULT_TYPE = "default";

    /**
     * Represents configuration of the health supervisor.
     */
    interface HealthCheckConfiguration extends HealthCheckInfo {
        /**
         * Gets map of attribute checkers where key is attribute name.
         *
         * @return Map of attribute checkers.
         */
        @Nonnull
        @Override
        FactoryMap<String, ? extends ScriptletConfiguration> getAttributeCheckers();
    }

    interface ScalingPolicyConfiguration extends ScalingPolicyInfo{
        void setVoteWeight(final double value);
    }

    interface MetricBasedScalingPolicyConfiguration extends MetricBasedScalingPolicyInfo, ScalingPolicyConfiguration {

        void setAggregationMethod(@Nonnull final ReduceOperation value);
        
        void setRange(@Nonnull final Range<Double> value);

        void setObservationTime(@Nonnull final Duration value);

        void setAttributeName(@Nonnull final String value);

        void setIncrementalVoteWeight(final boolean value);
    }

    interface CustomScalingPolicyConfiguration extends CustomScalingPolicyInfo, ScalingPolicyConfiguration {
    }

    /**
     * Represents configuration of automatic scaling (elasticity management)
     */
    interface AutoScalingConfiguration extends AutoScalingInfo{
        void setEnabled(final boolean value);
        void setCooldownTime(@Nonnull final Duration value);
        void setScalingSize(final int value);

        @Override
        @Nonnull
        FactoryMap<String, ? extends MetricBasedScalingPolicyConfiguration> getMetricBasedPolicies();

        @Nonnull
        @Override
        FactoryMap<String, ? extends CustomScalingPolicyConfiguration> getCustomPolicies();
    }

    interface ResourceDiscoveryConfiguration extends ResourceDiscoveryInfo{
        void setConnectionStringTemplate(final String value);
    }

    /**
     * Gets configuration of resource discovery.
     * @return Configuration of resource discovery.
     */
    @Nonnull
    @Override
    ResourceDiscoveryConfiguration getDiscoveryConfig();

    /**
     * Gets configuration of the health check.
     *
     * @return Configuration of the health checks.
     */
    @Nonnull
    @Override
    HealthCheckConfiguration getHealthCheckConfig();

    /**
     * Gets configuration of elasticity management process.
     * @return Elasticity manager.
     */
    @Nonnull
    @Override
    AutoScalingConfiguration getAutoScalingConfig();

    /**
     * Gets supervisor type.
     *
     * @return Supervisor type.
     * @see #DEFAULT_TYPE
     */
    @Override
    String getType();

    /**
     * Sets supervisor type.
     *
     * @param value Supervisor type.
     */
    @Override
    void setType(final String value);
}
