package com.itworks.snamp.configuration.diff;

import com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UpdateResourceAdapterInstancePatchImpl extends AbstractResourceAdapterInstancePatch implements UpdateResourceAdapterInstancePatch {

    UpdateResourceAdapterInstancePatchImpl(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final Map<String, ResourceAdapterConfiguration> adapters) {
        final ResourceAdapterConfiguration baselineConfig = adapters.get(getAdapterInstanceName());
        if(baselineConfig == null)
            adapters.put(getAdapterInstanceName(), getAdapter());
        else {
            baselineConfig.setAdapterName(getAdapter().getAdapterName());
            baselineConfig.getParameters().putAll(getAdapter().getParameters());
        }
    }
}
