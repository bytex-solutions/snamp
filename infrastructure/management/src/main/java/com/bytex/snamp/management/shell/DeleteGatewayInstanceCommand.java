package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.GatewayConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Deletes gateway instance.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-gateway",
    description = "Delete gateway instance from configuration")
@Service
public final class DeleteGatewayInstanceCommand extends ConfigurationCommand<GatewayConfiguration> {
    @Argument(index = 0, name = "instanceName", required = true, description = "The name of the gateway instance to remove")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String instanceName = "";

    public DeleteGatewayInstanceCommand(){
        super(GatewayConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends GatewayConfiguration> configuration, final StringBuilder output) {
        if(configuration.remove(instanceName) == null){
            output.append("Instance doesn't exist");
            return false;
        }
        else {
            output.append("Removed successfully");
            return true;
        }
    }
}
