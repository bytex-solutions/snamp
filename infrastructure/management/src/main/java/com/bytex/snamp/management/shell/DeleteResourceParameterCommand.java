package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes configuration parameter from managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-resource-param",
    description = "Deletes configuration parameter from managed resource")
public final class DeleteResourceParameterCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    @SpecialUse
    @Argument(index = 0, required = true, name = "resourceName", description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "parameter", required = true, description = "Configuration parameter to remove")
    private String paramName = "";

    public DeleteResourceParameterCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(resourceName)){
            configuration.get(resourceName).getParameters().remove(paramName);
            output.append("Resource modified successfully");
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
