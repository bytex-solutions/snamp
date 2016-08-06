package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AddResourceAdapterPatchIml extends AbstractResourceAdapterInstancePatch implements AddResourceAdapterPatch {
    AddResourceAdapterPatchIml(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        AbstractAgentConfiguration.copy(getEntity(),
                adapters.getOrAdd(getEntityID()));
    }
}
