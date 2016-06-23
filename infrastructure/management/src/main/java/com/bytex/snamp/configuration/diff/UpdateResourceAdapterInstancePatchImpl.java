package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class UpdateResourceAdapterInstancePatchImpl extends AbstractResourceAdapterInstancePatch implements UpdateResourceAdapterInstancePatch {

    UpdateResourceAdapterInstancePatchImpl(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        ResourceAdapterConfiguration baselineConfig = adapters.get(getAdapterInstanceName());
        if (baselineConfig == null)
            baselineConfig = adapters.getOrAdd(getAdapterInstanceName());
        AbstractAgentConfiguration.copy(getAdapter(), baselineConfig);
    }
}
