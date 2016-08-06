package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes configuration parameter of the adapter instance.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-adapter-param",
    description = "Delete configuration parameter of the adapter instance")
public final class DeleteAdapterParameterCommand extends ConfigurationCommand<ResourceAdapterConfiguration> {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the adapter instance to modify")
    @SpecialUse
    private String instanceName = "";

    @Argument(name = "parameter", index = 1, required = true, description = "Name of the parameter to remove")
    @SpecialUse
    private String paramName = "";

    public DeleteAdapterParameterCommand(){
        super(ResourceAdapterConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ResourceAdapterConfiguration> configuration, final StringBuilder output) {
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
