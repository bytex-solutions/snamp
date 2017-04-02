package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SupervisorInfo extends Map<String, String> {
    /**
     * Represents configuration of the health supervisor.
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
     * Gets configuration of the health check.
     * @return Configuration of the health checks.
     */
    @Nonnull
    HealthCheckInfo getHealthCheckConfig();
}
