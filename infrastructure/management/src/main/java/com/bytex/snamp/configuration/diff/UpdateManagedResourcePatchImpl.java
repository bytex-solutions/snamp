package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class UpdateManagedResourcePatchImpl extends AbstractManagedResourcePatch implements UpdateManagedResourcePatch {
    UpdateManagedResourcePatchImpl(final String resourceName, final ManagedResourceConfiguration resource) {
        super(resourceName, resource);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ManagedResourceConfiguration> baseline) {
        ManagedResourceConfiguration baselineConfig = baseline.get(getResourceName());
        if(baselineConfig == null)
            baselineConfig = baseline.getOrAdd(getResourceName());
        AbstractAgentConfiguration.copy(getResource(), baselineConfig);
    }
}
