package com.itworks.snamp.configuration.diff;

import com.itworks.snamp.configuration.AbstractAgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UpdateManagedResourcePatchImpl extends AbstractManagedResourcePatch implements UpdateManagedResourcePatch {
    UpdateManagedResourcePatchImpl(final String resourceName, final ManagedResourceConfiguration resource) {
        super(resourceName, resource);
    }

    @Override
    protected void applyTo(final Map<String, ManagedResourceConfiguration> baseline) {
        final ManagedResourceConfiguration baselineConfig = baseline.get(getResourceName());
        if(baselineConfig == null)
            baseline.put(getResourceName(), getResource());
        else AbstractAgentConfiguration.copy(getResource(), baselineConfig);
    }
}
