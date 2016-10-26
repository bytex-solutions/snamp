package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration;

/**
 * Represents an element that describes the difference between two configurations.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ConfigurationPatch {
    /**
     * Applies this patch to the baseline configuration.
     * @param baseline The configuration to modify.
     */
    void applyTo(final AgentConfiguration baseline);
}
