package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;

/**
 * Displays configuration of gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "gateway-instance",
    description = "Display configuration of gateway instance")
@Service
public final class GatewayInstanceInfoCommand extends GatewayConfigurationCommand {
    @Argument(index = 0, name = "instanceName", required = true, description = "Name of gateway instance to display")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String instanceName = "";

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final PrintWriter output) {
        if (configuration.containsKey(instanceName)) {
            final GatewayConfiguration gatewayInstanceConfig = configuration.get(instanceName);
            output.format("Instance Name: %s", instanceName).println();
            output.format("System Name: %s", gatewayInstanceConfig.getType()).println();
            output.println("Configuration parameters:");
            printParameters(gatewayInstanceConfig, output);
        } else
            output.append("Gateway instance doesn't exist");
        return false;
    }
}
