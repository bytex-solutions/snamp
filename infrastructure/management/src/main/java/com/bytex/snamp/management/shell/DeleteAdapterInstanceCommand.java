package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes adapter instance.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-adapter",
    description = "Deletes adapter instance from configuration")
public final class DeleteAdapterInstanceCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "instanceName", required = true, description = "The name of the adapter instance to remove")
    @SpecialUse
    private String instanceName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getResourceAdapters().remove(instanceName) == null){
            IOUtils.appendln(output, "Instance doesn't exist");
            return false;
        }
        else {
            IOUtils.appendln(output, "Removed successfully");
            return true;
        }
    }
}
