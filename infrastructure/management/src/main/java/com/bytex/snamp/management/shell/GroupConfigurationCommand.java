package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;

import javax.annotation.Nonnull;

abstract class GroupConfigurationCommand extends ConfigurationCommand<ManagedResourceGroupConfiguration> {
    @Nonnull
    @Override
    public final EntityMap<? extends ManagedResourceGroupConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getResourceGroups();
    }
}
