package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Represents configuration of the supervisor.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface SupervisorConfiguration extends TypedEntityConfiguration, ThreadPoolConfigurationSupport {
    /**
     * Represents default supervisor type.
     */
    String DEFAULT_TYPE = "default";

    /**
     * Represents configuration of the health supervisor.
     */
    interface HealthCheckConfiguration {
        /**
         * Gets map of attribute checkers where key is attribute name.
         *
         * @return Map of attribute checkers.
         */
        @Nonnull
        FactoryMap<String, ? extends ScriptletConfiguration> getAttributeCheckers();

        /**
         * Gets trigger called when status of the component will be changed.
         *
         * @return Trigger configuration.
         */
        @Nonnull
        ScriptletConfiguration getTrigger();
    }

    /**
     * Represents configuration of automatic scaling (elasticity management)
     */
    interface AutoScalingConfiguration{
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
         * Gets maximum number of resources in the cluster.
         * @return Maximum number of resources in the cluster.
         */
        int getMaxClusterSize();

        /**
         * Gets minimum number of resources in the cluster.
         * @return Minimum number of resources in the cluster.
         */
        int getMinClusterSize();
        void setEnabled(final boolean value);
        void setCooldownTime(@Nonnull final Duration value);
        void setScalingSize(final int value);
        void setMaxClusterSize(final int value);
        void setMinClusterSize(final int value);

        @Nonnull
        FactoryMap<String, ? extends ScriptletConfiguration> getPolicies();
    }

    interface ResourceDiscoveryConfiguration{
        /**
         * Gets template used to register resources in SNAMP using discovered information.
         * <p>
         *     Format of connection string template highly depends on implementation of supervisor.
         * @return Connection string template.
         */
        String getConnectionStringTemplate();

        void setConnectionStringTemplate(final String value);
    }

    /**
     * Gets configuration of resource discovery.
     * @return Configuration of resource discovery.
     */
    @Nonnull
    ResourceDiscoveryConfiguration getDiscoveryConfig();

    /**
     * Gets configuration of the health check.
     *
     * @return Configuration of the health checks.
     */
    @Nonnull
    HealthCheckConfiguration getHealthCheckConfig();

    /**
     * Gets configuration of elasticity management process.
     * @return Elasticity manager.
     */
    @Nonnull
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
