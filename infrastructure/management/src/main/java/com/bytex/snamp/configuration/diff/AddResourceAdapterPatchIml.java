package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class AddResourceAdapterPatchIml extends AbstractResourceAdapterInstancePatch implements AddResourceAdapterPatch {
    AddResourceAdapterPatchIml(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        AbstractAgentConfiguration.copy(getAdapter(),
                adapters.getOrAdd(getAdapterInstanceName()));
    }
}
