package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.*;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
abstract class AbstractManagedResourcePatch implements ManagedResourcePatch {
    private final String resourceName;
    private final ManagedResourceConfiguration resourceConfig;

    AbstractManagedResourcePatch(final String resourceName,
                                 final ManagedResourceConfiguration resource){
        this.resourceName = resourceName;
        this.resourceConfig = resource;
    }

    @Override
    public final String getResourceName() {
        return resourceName;
    }

    @Override
    public final ManagedResourceConfiguration getResource() {
        return resourceConfig;
    }

    protected abstract void applyTo(final EntityMap<? extends ManagedResourceConfiguration> baseline);

    /**
     * Applies this patch to the baseline configuration.
     *
     * @param baseline The configuration to modify.
     */
    @Override
    public final void applyTo(final AgentConfiguration baseline) {
        applyTo(baseline.getManagedResources());
    }
}
