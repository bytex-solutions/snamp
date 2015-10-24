package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Deletes attribute from managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-attribute",
    description = "Delete attribute from managed resource")
public final class DeleteAttributeCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the managed resource to modify")
    @SpecialUse
    private String resourceName = "";

    @Argument(index = 0, name = "userDefinedName", required = true, description = "User-defined name of the attribute to remove")
    @SpecialUse
    private String userDefinedName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName))
            if(configuration.getManagedResources().get(resourceName).getElements(AttributeConfiguration.class).remove(userDefinedName) == null){
                output.append("Attribute doesn't exist");
                return false;
            }
            else {
                output.append("Attribute deleted successfully");
                return true;
            }
        else {
            output.append("Resource doesn't exist");
            return true;
        }
    }
}
