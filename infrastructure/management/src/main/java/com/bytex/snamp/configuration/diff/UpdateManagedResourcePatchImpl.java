package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

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
