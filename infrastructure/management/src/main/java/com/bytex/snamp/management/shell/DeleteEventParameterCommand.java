package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Deletes configuration parameter from event.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-event-param",
    description = "Delete configuration parameter from event")
public final class DeleteEventParameterCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of event to modify")
    private String userDefinedName = "";

    @SpecialUse
    @Argument(index = 2, name = "parameter", required = true, description = "Name of parameter to remove")
    private String paramName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName))
            if(configuration.getManagedResources().get(resourceName).getElements(EventConfiguration.class).containsKey(userDefinedName)){
                configuration.getManagedResources()
                        .get(resourceName)
                        .getElements(EventConfiguration.class)
                        .get(userDefinedName)
                        .getParameters().remove(paramName);
                output.append("Event modified successfully");
                return true;
            }
            else {
                output.append("Event doesn't exist");
                return false;
            }
        else {
            output.append("Resource doesn't exist");
            return true;
        }
    }
}
