package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Shows list of configured managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "resources",
    description = "List of configured managed resources")
@Service
public final class ListOfResourcesCommand extends ManagedResourceConfigurationCommand {
    @Option(name = "-g", aliases = {"--group"}, description = "Filter resources by group name")
    private String groupName = "";

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final PrintWriter output) {
        for (final Map.Entry<String, ? extends ManagedResourceConfiguration> resource : configuration.entrySet())
            if (isNullOrEmpty(groupName) || Objects.equals(groupName, resource.getValue().getGroupName()))
                output.format("Resource: %s. Type: %s. Connection string: %s", resource.getKey(),
                        resource.getValue().getType(),
                        resource.getValue().getConnectionString()).println();
        return false;
    }
}
