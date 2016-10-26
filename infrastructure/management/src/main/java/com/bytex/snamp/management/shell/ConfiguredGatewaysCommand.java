package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.commands.Command;

import static com.bytex.snamp.management.shell.Utils.appendln;

/**
 * Prints list of configured gateway instances.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "gateway-instances",
        description = "List of configured gateway instances")
public final class ConfiguredGatewaysCommand extends ConfigurationCommand<GatewayConfiguration> {
    public ConfiguredGatewaysCommand(){
        super(GatewayConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        configuration.forEach((instance, config) -> appendln(output, "Instance: %s. Gateway: %s", instance, config.getType()));
        return false;
    }
}
