package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Deletes configuration parameter of the adapter instance.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-adapter-param",
    description = "Delete configuration parameter of the adapter instance")
public final class DeleteAdapterParameterCommand extends ConfigurationCommand {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the adapter instance to modify")
    @SpecialUse
    private String instanceName = "";

    @Argument(name = "parameter", index = 1, required = true, description = "Name of the parameter to remove")
    @SpecialUse
    private String paramName = "";

    public DeleteAdapterParameterCommand(){
        super(true);
    }

    @Override
    void doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getEntities(ResourceAdapterConfiguration.class).containsKey(instanceName)){
            configuration.getEntities(ResourceAdapterConfiguration.class).get(instanceName).getParameters().remove(paramName);
            output.append("Instance modified successfully");
        }
        else {
            output.append("Instance doesn't exist");
        }
    }
}
