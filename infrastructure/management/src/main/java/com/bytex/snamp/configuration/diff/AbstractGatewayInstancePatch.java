package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractGatewayInstancePatch implements GatewayInstancePatch {
    private final String gatewayInstanceName;
    private final GatewayConfiguration gatewayInstanceConfig;

    AbstractGatewayInstancePatch(final String gatewayInstanceName,
                                 final GatewayConfiguration gatewayInstance) {
        this.gatewayInstanceName = gatewayInstanceName;
        this.gatewayInstanceConfig = gatewayInstance;
    }

    @Override
    public final GatewayConfiguration getEntity() {
        return gatewayInstanceConfig;
    }

    @Override
    public final String getEntityID() {
        return gatewayInstanceName;
    }

    protected abstract void applyTo(final EntityMap<? extends GatewayConfiguration> gateways);

    /**
     * Applies this patch to the baseline configuration.
     *
     * @param baseline The configuration to modify.
     */
    @Override
    public final void applyTo(final AgentConfiguration baseline) {
        applyTo(baseline.getEntities(GatewayConfiguration.class));
    }
}