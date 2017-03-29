package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Deletes attribute from managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-attribute",
    description = "Delete attribute from managed resource")
@Service
public final class DeleteAttributeCommand extends ManagedResourceConfigurationCommand {
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the managed resource to modify")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String resourceName = "";

    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of the attribute to remove")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String userDefinedName = "";

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(resourceName))
            if(configuration.get(resourceName).getAttributes().remove(userDefinedName) == null){
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
