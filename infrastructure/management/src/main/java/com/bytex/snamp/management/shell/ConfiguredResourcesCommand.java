package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Shows list of configured managed resources.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "resources",
    description = "List of configured managed resources")
public final class ConfiguredResourcesCommand extends ConfigurationCommand {
    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        for(final Map.Entry<String, ManagedResourceConfiguration> resource: configuration.getManagedResources().entrySet())
            IOUtils.appendln(output, "Resource: %s. Type: %s. Connection string: %s", resource.getKey(),
                    resource.getValue().getConnectionType(),
                    resource.getValue().getConnectionString());
        return false;
    }
}
