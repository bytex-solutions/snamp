package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class GatewayConfigurationCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Nonnull
    @Override
    public final EntityMap<? extends GatewayConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getGateways();
    }
}
