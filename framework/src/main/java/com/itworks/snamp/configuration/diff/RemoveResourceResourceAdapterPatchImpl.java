package com.itworks.snamp.configuration.diff;

import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RemoveResourceResourceAdapterPatchImpl extends AbstractResourceAdapterInstancePatch implements RemoveResourceResourceAdapterPatch {


    RemoveResourceResourceAdapterPatchImpl(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final Map<String, ResourceAdapterConfiguration> adapters) {
        adapters.remove(getAdapterInstanceName());
    }
}
