package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;
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
