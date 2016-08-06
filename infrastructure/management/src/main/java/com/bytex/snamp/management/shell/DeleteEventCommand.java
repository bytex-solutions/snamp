package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Deletes event from resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-event",
    description = "Delete event from resource")
public final class DeleteEventCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of the event to remove")
    private String userDefinedName = "";

    public DeleteEventCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if (configuration.containsKey(resourceName))
            if (configuration.get(resourceName).getFeatures(EventConfiguration.class).remove(userDefinedName) == null) {
                output.append("Event doesn't exist");
                return false;
            } else {
                output.append("Event deleted successfully");
                return true;
            }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
