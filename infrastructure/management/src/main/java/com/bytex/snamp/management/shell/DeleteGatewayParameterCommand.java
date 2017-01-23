package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Deletes configuration parameter of the gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-gateway-param",
    description = "Delete configuration parameter of the gateway instance")
@Service
public final class DeleteGatewayParameterCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the gateway instance to modify")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String instanceName = "";

    @Argument(name = "parameter", index = 1, required = true, description = "Name of the parameter to remove")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String paramName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    public DeleteGatewayParameterCommand(){
        super(GatewayConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(instanceName)){
            configuration.get(instanceName).remove(paramName);
            output.append("Instance modified successfully");
            return true;
        }
        else {
            output.append("Instance doesn't exist");
            return false;
        }
    }
}
