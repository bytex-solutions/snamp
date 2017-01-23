package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

/**
 * Deletes configuration parameter from managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "delete-resource-param",
    description = "Deletes configuration parameter from managed resource")
@Service
public final class DeleteResourceParameterCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, required = true, name = "resourceName", description = "Name of resource to modify")
    private String resourceName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "parameter", required = true, description = "Configuration parameter to remove")
    private String paramName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    public DeleteResourceParameterCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(resourceName)){
            configuration.get(resourceName).remove(paramName);
            output.append("Resource modified successfully");
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
