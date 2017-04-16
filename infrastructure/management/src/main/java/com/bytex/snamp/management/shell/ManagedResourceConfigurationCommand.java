package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ManagedResourceConfigurationCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    @Nonnull
    @Override
    public final EntityMap<? extends ManagedResourceConfiguration> apply(@Nonnull AgentConfiguration owner) {
        return owner.getResources();
    }
}
