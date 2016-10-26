package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractThreadPoolPatch implements ThreadPoolPatch {
    private final String poolName;
    private final ThreadPoolConfiguration configuration;

    AbstractThreadPoolPatch(final String poolName, final ThreadPoolConfiguration configuration){
        this.poolName = Objects.requireNonNull(poolName);
        this.configuration = Objects.requireNonNull(configuration);
    }

    @Override
    public final String getEntityID() {
        return poolName;
    }

    @Override
    public final ThreadPoolConfiguration getEntity() {
        return configuration;
    }

    protected abstract void applyTo(final EntityMap<? extends ThreadPoolConfiguration> baseline);

    @Override
    public final void applyTo(final AgentConfiguration baseline) {
        applyTo(baseline.getEntities(ThreadPoolConfiguration.class));
    }
}
