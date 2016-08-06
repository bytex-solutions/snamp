package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes adapter instance.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-adapter",
    description = "Delete adapter instance from configuration")
public final class DeleteAdapterInstanceCommand extends ConfigurationCommand<ResourceAdapterConfiguration> {
    @Argument(index = 0, name = "instanceName", required = true, description = "The name of the adapter instance to remove")
    @SpecialUse
    private String instanceName = "";

    public DeleteAdapterInstanceCommand(){
        super(ResourceAdapterConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ResourceAdapterConfiguration> configuration, final StringBuilder output) {
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
