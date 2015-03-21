package com.itworks.snamp.configuration.diff;

import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceAdapterInstancePatch extends ConfigurationPatch {
    ResourceAdapterConfiguration getAdapter();
    String getAdapterInstanceName();
}
