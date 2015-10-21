package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes configuration parameter of the adapter instance.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-adapter-param",
    description = "Deletes configuration parameter of the adapter instance")
public final class DeleteAdapterParameterCommand extends ConfigurationCommand {
    @Argument(name = "instanceName", index = 0, required = true, description = "Name of the adapter instance to modify")
    @SpecialUse
    private String instanceName = "";

    @Argument(name = "parameter", index = 1, required = true, description = "Name of the parameter to remove")
    @SpecialUse
    private String paramName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getResourceAdapters().containsKey(instanceName)){
            configuration.getResourceAdapters().get(instanceName).getParameters().remove(paramName);
            IOUtils.appendln(output, "Instance modified successfully");
            return true;
        }
        else {
            IOUtils.appendln(output, "Instance doesn't exist");
            return false;
        }
    }
}
