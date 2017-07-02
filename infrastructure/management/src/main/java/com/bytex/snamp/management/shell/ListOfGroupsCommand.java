package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;

/**
 * Prints list of configured groups.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "resource-groups",
        description = "List of configured resource groups")
@Service
public class ListOfGroupsCommand extends GroupConfigurationCommand {
    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceGroupConfiguration> configuration, final PrintWriter output) throws Exception {
        configuration.forEach((instance, config) -> output.format("Group name: %s. Type: %s", instance, config.getType()).println());
        return false;
    }
}
