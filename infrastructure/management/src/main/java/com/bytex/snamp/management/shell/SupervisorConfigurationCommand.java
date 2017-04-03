package com.bytex.snamp.management.shell;


import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.SupervisorConfiguration;

import javax.annotation.Nonnull;

abstract class SupervisorConfigurationCommand extends ConfigurationCommand<SupervisorConfiguration> {
    @Nonnull
    @Override
    public final EntityMap<? extends SupervisorConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getSupervisors();
    }
}
