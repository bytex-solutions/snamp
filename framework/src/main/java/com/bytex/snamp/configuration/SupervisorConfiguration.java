package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;

/**
 * Represents configuration of the component watcher.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface SupervisorConfiguration extends TypedEntityConfiguration {
    String DEFAULT_TYPE = "DEFAULT";

    /**
     * Represents configuration of the health supervisor.
     */
    interface HealthCheckConfiguration {
        /**
         * Gets map of attribute checkers where key is attribute name.
         *
         * @return Map of attribute checkers.
         */
        FactoryMap<String, ? extends ScriptletConfiguration> getAttributeCheckers();

        /**
         * Gets trigger called when status of the component will be changed.
         *
         * @return Trigger configuration.
         */
        ScriptletConfiguration getTrigger();
    }

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

    /**
     * Gets configuration of the health check.
     * @return Configuration of the health checks.
     */
    HealthCheckConfiguration getHealthCheckConfig();
}
