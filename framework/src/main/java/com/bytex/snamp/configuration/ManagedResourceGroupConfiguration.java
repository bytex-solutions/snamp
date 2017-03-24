package com.bytex.snamp.configuration;

import com.bytex.snamp.FactoryMap;

/**
 * Represents group of managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ManagedResourceGroupConfiguration extends ManagedResourceTemplate {
    /**
     * Represents configuration of the health supervisor.
     */
    interface HealthSupervisorConfiguration {
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
}
