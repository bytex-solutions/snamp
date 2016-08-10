package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes configuration parameter of the adapter instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-gateway-param",
    description = "Delete configuration parameter of the gateway instance")
public final class DeleteGatewayParameterCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the gateway instance to modify")
    @SpecialUse
    private String instanceName = "";

    @Argument(name = "parameter", index = 1, required = true, description = "Name of the parameter to remove")
    @SpecialUse
    private String paramName = "";

    public DeleteGatewayParameterCommand(){
        super(GatewayConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(instanceName)){
            configuration.get(instanceName).getParameters().remove(paramName);
            output.append("Instance modified successfully");
            return true;
        }
        else {
            output.append("Instance doesn't exist");
            return false;
        }
    }
}
