package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Prints list of configured gateway instances.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "gateway-instances",
        description = "List of configured gateway instances")
@Service
public final class ConfiguredGatewaysCommand extends ConfigurationCommand<GatewayConfiguration> {

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        configuration.forEach((instance, config) -> appendln(output, "Instance: %s. Gateway: %s", instance, config.getType()));
        return false;
    }

    @Nonnull
    @Override
    public EntityMap<? extends GatewayConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getGateways();
    }
}
