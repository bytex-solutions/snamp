package com.bytex.snamp.configuration;

import com.bytex.snamp.core.FrameworkService;

/**
 * Represents SNAMP configuration manager that is accessible as OSGi service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationManager extends FrameworkService {

    /**
     * Returns the currently loaded configuration.
     * @return The currently loaded configuration.
     */
    AgentConfiguration getCurrentConfiguration();

    /**
     * Reload agent configuration from the persistent storage.
     */
    void reload();

    /**
     * Dumps the agent configuration into the persistent storage.
     */
    void sync();
}
