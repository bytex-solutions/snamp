package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Prints list of configured groups.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "resource-groups",
        description = "List of configured resource groups")
@Service
public class ListOfGroupsCommand extends GroupConfigurationCommand {
    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceGroupConfiguration> configuration, final StringBuilder output) throws Exception {
        configuration.forEach((instance, config) -> appendln(output, "Group name: %s. Type: %s", instance, config.getType()));
        return false;
    }
}
