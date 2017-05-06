package com.bytex.snamp.configuration;

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
        int getScalingSize();

        /**
         * Gets scaling policies.
         * @return A map of scaling policies.
         * @implSpec Key is a name of policy.
         */
        @Nonnull
        Map<String, ? extends ScriptletConfiguration> getPolicies();
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

    @Nonnull
    AutoScalingInfo getAutoScalingConfig();
}
