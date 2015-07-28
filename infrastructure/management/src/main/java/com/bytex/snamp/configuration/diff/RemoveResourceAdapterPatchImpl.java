package com.bytex.snamp.configuration.diff;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RemoveResourceAdapterPatchImpl extends AbstractResourceAdapterInstancePatch implements RemoveResourceAdapterPatch {


    RemoveResourceAdapterPatchImpl(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final Map<String, ResourceAdapterConfiguration> adapters) {
        adapters.remove(getAdapterInstanceName());
    }
}
