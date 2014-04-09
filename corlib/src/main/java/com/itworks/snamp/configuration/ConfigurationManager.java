package com.itworks.snamp.configuration;

import com.itworks.snamp.core.PlatformService;

/**
 * Represents SNAMP configuration manager that is accessible as OSGi service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationManager extends PlatformService {
    /**
     * Represents system property that contains a path to the SNAMP configuration file.
     */
    String CONFIGURATION_FILE_PROPERTY = "com.itworks.snamp.configuration.file";

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
