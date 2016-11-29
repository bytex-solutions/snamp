package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AddGatewayPatchIml extends AbstractGatewayInstancePatch implements AddGatewayInstancePatch {
    AddGatewayPatchIml(final String gatewayInstanceName, final GatewayConfiguration gatewayInstanceConfig) {
        super(gatewayInstanceName, gatewayInstanceConfig);
    }

    @Override
    protected void applyTo(final EntityMap<? extends GatewayConfiguration> gateways) {
        AbstractAgentConfiguration.copy(getEntity(),
                gateways.getOrAdd(getEntityID()));
    }
}
