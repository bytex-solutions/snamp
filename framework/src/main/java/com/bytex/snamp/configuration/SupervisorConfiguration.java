package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;

import javax.annotation.Nonnull;

/**
 * Represents configuration of the supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SupervisorConfiguration extends TypedEntityConfiguration, SupervisorInfo {
    String DEFAULT_TYPE = "DEFAULT";

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

    /**
     * Gets configuration of the health check.
     *
     * @return Configuration of the health checks.
     */
    @Nonnull
    @Override
    HealthCheckConfiguration getHealthCheckConfig();

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
