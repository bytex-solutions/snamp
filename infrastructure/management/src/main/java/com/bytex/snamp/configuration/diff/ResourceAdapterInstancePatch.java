package com.bytex.snamp.configuration.diff;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ResourceAdapterInstancePatch extends ConfigurationPatch {
    ResourceAdapterConfiguration getAdapter();
    String getAdapterInstanceName();
}
