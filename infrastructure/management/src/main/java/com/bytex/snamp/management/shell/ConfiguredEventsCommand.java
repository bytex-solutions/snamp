package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Shows list of resource events.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "events",
    description = "List of configured attributes assigned to the resource")
public final class ConfiguredEventsCommand extends ConfigurationCommand {
    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of resource to explore")
    private String resourceName = "";

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName)) {
            for (final Map.Entry<String, EventConfiguration> entry : configuration.getManagedResources().get(resourceName).getElements(EventConfiguration.class).entrySet()) {
                IOUtils.appendln(output, "UserDefinedName: %s, Category: %s", entry.getKey(), entry.getValue().getCategory());
            }
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
