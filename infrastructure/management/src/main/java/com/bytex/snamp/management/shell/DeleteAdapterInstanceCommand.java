package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
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
public final class DeleteAdapterInstanceCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "instanceName", required = true, description = "The name of the adapter instance to remove")
    @SpecialUse
    private String instanceName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getResourceAdapters().remove(instanceName) == null){
            output.append("Instance doesn't exist");
            return false;
        }
        else {
            output.append("Removed successfully");
            return true;
        }
    }
}
