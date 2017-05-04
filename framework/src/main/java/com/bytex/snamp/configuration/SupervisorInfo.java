package com.bytex.snamp.configuration;

import com.bytex.snamp.moa.ReduceOperation;
import com.google.common.collect.Range;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SupervisorInfo extends ThreadPoolConfigurationSupport {
    /**
     * Represents information about health check configuration.
     */
    interface HealthCheckInfo {
        /**
         * Gets map of attribute checkers where key is attribute name.
         *
         * @return Map of attribute checkers.
         */
        @Nonnull
        Map<String, ? extends ScriptletConfiguration> getAttributeCheckers();

        /**
         * Gets trigger called when status of the component will be changed.
         *
         * @return Trigger configuration.
         */
        @Nonnull
        ScriptletConfiguration getTrigger();
    }

    /**
     * Represents information about resource discovery.
     */
    interface ResourceDiscoveryInfo{
        /**
         * Gets template used to register resources in SNAMP using discovered information.
         * <p>
         *     Format of connection string template highly depends on implementation of supervisor.
         * @return Connection string template.
         */
        String getConnectionStringTemplate();
    }

    /**
     * Represents scaling policy.
     */
    interface ScalingPolicyInfo{
        /**
         * Gets weight of the vote associated with this policy.
         * @return Gets weight of vote.
         */
        double getVoteWeight();
    }

    /**
     * Represents programmatically defined scaling policy.
     */
    interface CustomScalingPolicyInfo extends ScalingPolicyInfo, ScriptletConfiguration{

    }

    /**
     * Represents scaling policy based on values of some metrics.
     */
    interface MetricBasedScalingPolicyInfo extends ScalingPolicyInfo{
        /**
         * Indicates that weight of the vote will be increased proportionally to actual observation time.
         * @return {@literal true}, if vote weight can be increased proportionally to actual observation time; {@literal false} if weight is constant.
         */
        boolean isIncrementalVoteWeight();

        /**
         * Gets aggregation method used to obtain summary metric value from a set of resources.
         * @return Aggregation method.
         */
        @Nonnull
        ReduceOperation getAggregationMethod();

        /**
         * Gets operational interval of the metric values.
         * @return Operational interval of the metric values.
         */
        @Nonnull
        Range<Double> getRange();

        /**
         * Gets observation period used to enforce policy.
         * <p>
         *     When value of the metric is observing during the specified time then this p
         * @return Observation 
         */
        Duration getObservationTime();
    }

    /**
     * Represents information about automatic scaling
     */
    interface AutoScalingInfo{
        /**
         * Is auto-scaling enabled?
         * @return {@literal true}, if auto-scaling is enabled; otherwise, {@literal false}.
         */
        boolean isEnabled();

        /**
         * Gets period that helps to ensure that Elasticity Manager doesn't launch or terminate additional instances before the previous scaling activity takes effect.
         * @return Cooldown period.
         * @see <a href="http://docs.aws.amazon.com/autoscaling/latest/userguide/Cooldown.html">Auto Scaling Cooldowns</a>
         */
        @Nonnull
        Duration getCooldownTime();

        /**
         * Gets number of instances used to enlarge or shrink cluster.
         * @return Scale size.
         */
        int getScale();

        /**
         * Gets scaling policies based on values of the metrics.
         * @return A map of scaling policies based on values of the metrics.
         * @implSpec Key is a name of attribute.
         */
        @Nonnull
        Map<String, ? extends MetricBasedScalingPolicyInfo> getMetricBasedPolicies();

        /**
         * Gets custom scaling policies.
         * @return A map with custom policies.
         * @implSpec Key is a name of policy.
         */
        @Nonnull
        Map<String, ? extends CustomScalingPolicyInfo> getCustomPolicies();
    }

    /**
     * Gets configuration of the health check.
     * @return Configuration of the health checks.
     */
    @Nonnull
    HealthCheckInfo getHealthCheckConfig();

    /**
     * Gets configuration of resource discovery.
     * @return Configuration of resource discovery.
     */
    @Nonnull
    ResourceDiscoveryInfo getDiscoveryConfig();
}
