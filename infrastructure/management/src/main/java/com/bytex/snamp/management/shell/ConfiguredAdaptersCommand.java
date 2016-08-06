package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Command;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.management.shell.Utils.appendln;

/**
 * Prints list of configured adapter instances.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "adapter-instances",
        description = "List of configured adapter instances")
public final class ConfiguredAdaptersCommand extends ConfigurationCommand {

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        configuration.getEntities(ResourceAdapterConfiguration.class).entrySet().forEach(adapter -> appendln(output, "Instance: %s. Adapter: %s", adapter.getKey(), adapter.getValue().getAdapterName()));
        return false;
    }
}
