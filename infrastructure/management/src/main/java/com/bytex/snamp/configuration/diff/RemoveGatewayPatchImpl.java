package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class RemoveGatewayPatchImpl extends AbstractGatewayInstancePatch implements RemoveGatewayPatch {

    RemoveGatewayPatchImpl(final String gatewayInstanceName, final GatewayConfiguration gatewayInstance) {
        super(gatewayInstanceName, gatewayInstance);
    }

    @Override
    protected void applyTo(final EntityMap<? extends GatewayConfiguration> gateways) {
        gateways.remove(getEntityID());
    }
}
