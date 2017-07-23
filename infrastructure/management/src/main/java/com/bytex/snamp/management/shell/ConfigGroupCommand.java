package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.util.Arrays;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Configures resource groups.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        name = "configure-resource-group",
        description = "Configure managed resource groups")
@Service
public final class ConfigGroupCommand extends GroupConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group")
    private String groupName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "connectionType", required = false, description = "Name of the connector")
    private String connectionType = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete resource")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param, --parameter"}, required = false, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceGroupConfiguration> groups, final PrintWriter output) throws Exception {
        if (del)
            groups.remove(groupName);
        else {
            final ManagedResourceGroupConfiguration resource = groups.getOrAdd(groupName);
            //setup connection type
            if (!isNullOrEmpty(connectionType))
                resource.setType(connectionType);
            //setup parameters
            resource.putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(resource::remove);
        }
        output.println("Group configured successfully");
        return true;
    }
}
