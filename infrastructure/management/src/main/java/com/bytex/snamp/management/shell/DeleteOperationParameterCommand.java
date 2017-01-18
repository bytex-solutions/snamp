package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-operation-param",
    description = "Delete configuration parameter from operation")
@Service
public final class DeleteOperationParameterCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of event to modify")
    private String userDefinedName = "";

    @SpecialUse
    @Argument(index = 2, name = "parameter", required = true, description = "Name of operation to remove")
    private String paramName = "";

    public DeleteOperationParameterCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(resourceName))
            if(configuration.get(resourceName).getFeatures(OperationConfiguration.class).containsKey(userDefinedName)){
                configuration
                        .get(resourceName)
                        .getFeatures(OperationConfiguration.class)
                        .get(userDefinedName)
                        .remove(paramName);
                output.append("Operation modified successfully");
                return true;
            }
            else {
                output.append("Operation doesn't exist");
                return false;
            }
        else {
            output.append("Resource doesn't exist");
            return true;
        }
    }
}
