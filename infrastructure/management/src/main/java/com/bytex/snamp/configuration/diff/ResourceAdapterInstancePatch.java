package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface ResourceAdapterInstancePatch extends ConfigurationPatch {
    ResourceAdapterConfiguration getAdapter();
    String getAdapterInstanceName();
}
