package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractResourceAdapterInstancePatch implements ResourceAdapterInstancePatch {
    private final String adapterInstance;
    private final ResourceAdapterConfiguration adapterConfig;

    AbstractResourceAdapterInstancePatch(final String adapterInstanceName,
                                         final ResourceAdapterConfiguration adapter) {
        this.adapterInstance = adapterInstanceName;
        this.adapterConfig = adapter;
    }

    @Override
    public final ResourceAdapterConfiguration getAdapter() {
        return adapterConfig;
    }

    @Override
    public final String getAdapterInstanceName() {
        return adapterInstance;
    }

    protected abstract void applyTo(final EntityMap<? extends ResourceAdapterConfiguration> adapters);

    /**
     * Applies this patch to the baseline configuration.
     *
     * @param baseline The configuration to modify.
     */
    @Override
    public final void applyTo(final AgentConfiguration baseline) {
        applyTo(baseline.getResourceAdapters());
    }
}
