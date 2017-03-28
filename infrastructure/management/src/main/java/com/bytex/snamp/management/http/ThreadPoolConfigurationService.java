package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.bytex.snamp.management.http.model.ThreadPoolDataObject;

import javax.annotation.Nonnull;

/**
 * Represents configuration service for
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ThreadPoolConfigurationService extends AbstractEntityConfigurationService<ThreadPoolConfiguration, ThreadPoolDataObject> {
    @Override
    protected ThreadPoolDataObject toDataTransferObject(final ThreadPoolConfiguration entity) {
        return new ThreadPoolDataObject(entity);
    }

    @Nonnull
    @Override
    public EntityMap<? extends ThreadPoolConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getThreadPools();
    }
}
