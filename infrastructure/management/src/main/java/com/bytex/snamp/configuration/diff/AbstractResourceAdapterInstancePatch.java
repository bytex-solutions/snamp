package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractResourceAdapterInstancePatch implements ResourceAdapterInstancePatch {
    private final String adapterInstance;
    private final GatewayConfiguration adapterConfig;

    AbstractResourceAdapterInstancePatch(final String adapterInstanceName,
                                         final GatewayConfiguration adapter) {
        this.adapterInstance = adapterInstanceName;
        this.adapterConfig = adapter;
    }

    @Override
    public final GatewayConfiguration getEntity() {
        return adapterConfig;
    }

    @Override
    public final String getEntityID() {
        return adapterInstance;
    }

    protected abstract void applyTo(final EntityMap<? extends GatewayConfiguration> adapters);

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
