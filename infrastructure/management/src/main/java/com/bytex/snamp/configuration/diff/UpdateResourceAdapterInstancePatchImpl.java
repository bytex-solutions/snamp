package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class UpdateResourceAdapterInstancePatchImpl extends AbstractResourceAdapterInstancePatch implements UpdateResourceAdapterInstancePatch {

    UpdateResourceAdapterInstancePatchImpl(final String adapterInstanceName, final GatewayConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final EntityMap<? extends GatewayConfiguration> adapters) {
        GatewayConfiguration baselineConfig = adapters.getOrAdd(getEntityID());
        AbstractAgentConfiguration.copy(getEntity(), baselineConfig);
    }
}
