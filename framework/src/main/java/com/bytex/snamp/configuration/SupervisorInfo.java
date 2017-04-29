package com.bytex.snamp.configuration;

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
     * Represents behavior model of the metric.
     */
    enum MetricBehaviorModel{
        /**
         * Behavior is unknown
         */
        UNKNOWN,

        /**
         * Values of the metric represents Gaussian distribution.
         * <p>
         *  Mean value is the most probable.
         */
        GAUSSIAN,

        /**
         * Value of the metric represents Poisson distribution.
         * <p>
         *  Lesser value is the most probable.
         */
        POISSON
    }

    /**
     * Represents scaling policy.
     */
    interface ScalingPolicyInfo{
        /**
         * Gets weight of the vote associated with this policy.
         * @return Gets weight of vote. Min is 1.
         */
        int getVoteWeight();
    }

    interface MetricBasedScalingPolicyInfo extends ScalingPolicyInfo{
        /**
         * Gets behavior model of the metric.
         * @return Behavior model of the metric.
         */
        @Nonnull
        MetricBehaviorModel getBehavior();

        /**
         * Gets operational interval of the metric values.
         * @return Operational interval of the metric values.
         */
        @Nonnull
        Range<?> getRange();
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
         */
        @Nonnull
        Map<String, ? extends MetricBasedScalingPolicyInfo> getMetricBasedPolicies();
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
