package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import javax.annotation.Nonnull;

/**
 * Provides abstract class for all thread pool management commands.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.1
 */
abstract class ThreadPoolCommand extends ConfigurationCommand<ThreadPoolConfiguration> {
    @Nonnull
    @Override
    public final EntityMap<? extends ThreadPoolConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getThreadPools();
    }
}
