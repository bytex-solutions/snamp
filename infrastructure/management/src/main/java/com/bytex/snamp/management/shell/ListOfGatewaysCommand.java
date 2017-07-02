package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;


/**
 * Prints list of configured gateway instances.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "gateway-instances",
        description = "List of configured gateway instances")
@Service
public final class ListOfGatewaysCommand extends GatewayConfigurationCommand {

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final PrintWriter output) {
        configuration.forEach((instance, config) -> output.format("Instance: %s. Gateway: %s", instance, config.getType()).println());
        return false;
    }
}
