package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Deletes event from resource.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-event",
    description = "Delete event from resource")
public final class DeleteEventCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of the event to remove")
    private String userDefinedName = "";

    public DeleteEventCommand(){
        super(true);
    }

    @Override
    void doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if (configuration.getEntities(ManagedResourceConfiguration.class).containsKey(resourceName))
            if (configuration.getEntities(ManagedResourceConfiguration.class).get(resourceName).getFeatures(EventConfiguration.class).remove(userDefinedName) == null) {
                output.append("Event doesn't exist");
            } else {
                output.append("Event deleted successfully");
            }
        else {
            output.append("Resource doesn't exist");
        }
    }
}
