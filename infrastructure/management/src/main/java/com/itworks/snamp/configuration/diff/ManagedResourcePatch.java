package com.itworks.snamp.configuration.diff;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedResourcePatch extends ConfigurationPatch {
    String getResourceName();
    ManagedResourceConfiguration getResource();
}
