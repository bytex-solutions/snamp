package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Provides list of configured supervisors.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "supervisors",
        description = "List of configured resource groups")
@Service
public class ListOfSupervisorsCommand extends SupervisorConfigurationCommand {
    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> configuration, final StringBuilder output) throws Exception {
        configuration.forEach((instance, config) -> appendln(output, "Group name: %s. Supervisor type: %s", instance, config.getType()));
        return false;
    }
}
