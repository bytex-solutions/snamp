package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * Represents configuration of the component watcher.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ManagedResourceGroupWatcherConfiguration extends EntityConfiguration {

    /**
     * Gets map of attribute checkers where key is attribute name.
     * @return Map of attribute checkers.
     */
    Map<String, ScriptletConfiguration> getAttributeCheckers();

    /**
     * Gets trigger called when status of the component will be changed.
     * @return Trigger configuration.
     */
    ScriptletConfiguration getTrigger();

    /**
     * Sets trigger called when status of the component will be changed.
     * @param scriptlet Trigger configuration.
     */
    void setTrigger(final ScriptletConfiguration scriptlet);
}
