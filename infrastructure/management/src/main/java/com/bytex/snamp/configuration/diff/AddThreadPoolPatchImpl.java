package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AbstractAgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AddThreadPoolPatchImpl extends AbstractThreadPoolPatch implements AddThreadPoolPatch {
    AddThreadPoolPatchImpl(final String poolName, final ThreadPoolConfiguration configuration) {
        super(poolName, configuration);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ThreadPoolConfiguration> baseline) {
        AbstractAgentConfiguration.copy(getEntity(), baseline.getOrAdd(getEntityID()));
    }
}
