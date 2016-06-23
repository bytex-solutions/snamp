package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Deletes operation from managed resource.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-operation",
    description = "Delete operation from managed resource")
public final class DeleteOperationCommand extends ConfigurationCommand {
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the managed resource to modify")
    @SpecialUse
    private String resourceName = "";

    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of the operation to remove")
    @SpecialUse
    private String userDefinedName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getEntities(ManagedResourceConfiguration.class).containsKey(resourceName))
            if(configuration.getEntities(ManagedResourceConfiguration.class).get(resourceName).getFeatures(OperationConfiguration.class).remove(userDefinedName) == null){
                output.append("Operation doesn't exist");
                return false;
            }
            else {
                output.append("Operation deleted successfully");
                return true;
            }
        else {
            output.append("Resource doesn't exist");
            return true;
        }
    }
}
