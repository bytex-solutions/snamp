package com.bytex.snamp.configuration.diff;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ManagedResourcePatch extends ConfigurationPatch {
    String getResourceName();
    ManagedResourceConfiguration getResource();
}
