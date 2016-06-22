package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Deletes configuration parameter from managed resource.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-resource-param",
    description = "Deletes configuration parameter from managed resource")
public final class DeleteResourceParameterCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, required = true, name = "resourceName", description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "parameter", required = true, description = "Configuration parameter to remove")
    private String paramName = "";

    public DeleteResourceParameterCommand(){
        super(true);
    }

    @Override
    void doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getEntities(ManagedResourceConfiguration.class).containsKey(resourceName)){
            configuration.getEntities(ManagedResourceConfiguration.class).get(resourceName).getParameters().remove(paramName);
            output.append("Resource modified successfully");
        }
        else {
            output.append("Resource doesn't exist");
        }
    }
}
